package si.result.eearly.service.postgres;

import java.util.function.Supplier;

public interface LockingManager {
	<T> T withLock(String lockString, Supplier<T> codeToExecute);

	void withLock(String lockString, Runnable codeToExecute);
}
