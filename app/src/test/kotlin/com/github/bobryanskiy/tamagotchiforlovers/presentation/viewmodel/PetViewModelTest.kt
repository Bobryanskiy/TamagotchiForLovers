package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class PetViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private fun createViewModel(): PetViewModel {
        return PetViewModel(
            savedStateHandle = SavedStateHandle(mapOf("petId" to "pet-1")),
            petRepository = mock(),
            pairRepository = mock(),
            applyActionUseCase = mock(),
            checkPetLifeDecayUseCase = mock(),
            accessController = mock(),
            notificationsCoordinator = mock(),
            taskGenerator = mock(),
            logger = mock(),
            calculateLiveStatsUseCase = mock(),
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `onActionRequested should open dialog with action`() = runTest {
        val viewModel = createViewModel()

        viewModel.onActionRequested(PetAction.Feed)

        val dialog = viewModel.dialogState.value
        assertEquals(PetAction.Feed, dialog?.action)
        assertEquals(false, dialog?.isProcessing)
    }

    @Test
    fun `onDialogDismiss should close dialog`() = runTest {
        val viewModel = createViewModel()
        viewModel.onActionRequested(PetAction.Play)

        viewModel.onDialogDismiss()

        assertNull(viewModel.dialogState.value)
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        val viewModel = createViewModel()

        assertEquals(PetUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `initial pair state should be Loading`() = runTest {
        val viewModel = createViewModel()

        assertEquals(PairButtonState.Loading, viewModel.pairButtonState.value)
    }
}
