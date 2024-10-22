package ru.astrainteractive.astratemplate.gui.sample

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.bukkit.ChatColor
import org.bukkit.event.inventory.ClickType
import ru.astrainteractive.astralibs.async.CoroutineFeature
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.astratemplate.api.local.dao.LocalDao
import ru.astrainteractive.astratemplate.api.local.model.UserModel
import ru.astrainteractive.astratemplate.gui.api.ItemStackSpigotAPI
import ru.astrainteractive.astratemplate.gui.domain.GetRandomColorUseCase
import ru.astrainteractive.astratemplate.gui.domain.SetDisplayNameUseCase
import ru.astrainteractive.astratemplate.gui.sample.SampleGuiComponent.Model
import kotlin.random.Random

/**
 * MVVM/MVI technique
 */
internal class DefaultSampleGUIComponent(
    private val localDao: LocalDao,
    private val itemStackSpigotAPi: ItemStackSpigotAPI,
    private val getRandomColorUseCase: GetRandomColorUseCase,
    private val setDisplayNameUseCase: SetDisplayNameUseCase
) : CoroutineFeature by CoroutineFeature.Default(Dispatchers.IO),
    SampleGuiComponent,
    Logger by JUtiltLogger("AstraTemplate-DefaultSampleGUIComponent") {

    override val model = MutableStateFlow<Model>(Model.Loading)

    override val randomColor: ChatColor
        get() = getRandomColorUseCase.invoke().color

    private fun getRandomUser(): UserModel {
        return UserModel(
            id = -1,
            discordId = "id${Random.nextInt(20000)}",
            minecraftUUID = "mine${Random.nextInt(5000)}"
        )
    }

    override fun onModeChange() {
        launch(Dispatchers.IO) {
            when (model.value) {
                Model.Loading -> return@launch
                is Model.Items -> loadUsersModel()
                is Model.Users -> loadItemsModel()
            }
        }
    }

    override fun onItemClicked(slot: Int, clickType: ClickType) {
        when (val state = model.value) {
            Model.Loading -> Unit
            is Model.Items -> {
                onItemStackClicked(slot)
            }

            is Model.Users -> {
                onPlayerHeadClicked(slot, clickType)
            }
        }
    }

    override fun onAddUserClicked() {
        launch(Dispatchers.IO) {
            localDao.insertUser(getRandomUser())
            loadUsersModel()
        }
    }

    private fun onPlayerHeadClicked(slot: Int, clickType: ClickType) {
        val state = model.value as? Model.Users ?: return
        val users = state.users
        val user = users.getOrNull(slot) ?: return
        launch(Dispatchers.IO) {
            when (clickType) {
                ClickType.MIDDLE -> localDao.updateUser(user)
                ClickType.LEFT -> localDao.deleteUser(user)
                else -> localDao.insertRating(user)
            }
            loadUsersModel()
        }
    }

    private fun onItemStackClicked(slot: Int) {
        val state = model.value as? Model.Items ?: return

        val input = SetDisplayNameUseCase.Input(
            items = state.items,
            index = slot
        )

        model.update {
            state.copy(items = setDisplayNameUseCase.invoke(input).items)
        }
    }

    private suspend fun loadItemsModel() {
        model.update { Model.Items(itemStackSpigotAPi.randomItemStackList()) }
    }

    private suspend fun loadUsersModel() {
        model.update { Model.Users(localDao.getAllUsers()) }
    }

    override fun onUiCreated() {
        launch(Dispatchers.IO) {
            delay(1000)
            loadItemsModel()
        }
    }
}
