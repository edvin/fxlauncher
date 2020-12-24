package fxlauncher.model.lifecycle;

import static fxlauncher.model.lifecycle.LifecyclePhase.STARTUP;
import static fxlauncher.model.lifecycle.LifecyclePhase.SYNC_ARTIFACTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LifecyclePhaseTest {

	@Test
	@DisplayName("Test set-current-phase")
	public void testSetCurrent() {
		LifecyclePhase.setCurrent(STARTUP);
		assertEquals(LifecyclePhase.current, STARTUP);

		LifecyclePhase.setCurrent(SYNC_ARTIFACTS);
		assertEquals(LifecyclePhase.current, SYNC_ARTIFACTS);
	}

	@ParameterizedTest(name = "Phase {0} notifies listeners on entry")
	@EnumSource(LifecyclePhase.class)
	@DisplayName("Test phase-entry listeners")
	public void testEnterPhaseListeners(LifecyclePhase phase) {
		LifecycleListener listener = mock(LifecycleListener.class);
		phase.registerEnterListener(listener);

		LifecyclePhase.setCurrent(phase);
		verify(listener, times(1)).notifyListener();
	}

	@ParameterizedTest(name = "Phase {0} notifies listeners on exit")
	@EnumSource(LifecyclePhase.class)
	@DisplayName("Test phase-entry listeners")
	public void testExitPhaseListeners(LifecyclePhase phase) {
		LifecycleListener listener = mock(LifecycleListener.class);
		LifecyclePhase.current.registerExitListener(listener);

		LifecyclePhase.setCurrent(phase);
		verify(listener, times(1)).notifyListener();
	}
}