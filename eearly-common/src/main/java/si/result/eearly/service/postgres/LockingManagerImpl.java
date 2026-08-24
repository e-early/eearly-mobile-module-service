package si.result.eearly.service.postgres;

import java.sql.PreparedStatement;
import java.util.function.Supplier;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import si.result.eearly.domain.exception.LockCouldNotBeAcquiredException;

@Slf4j
@AllArgsConstructor
@Repository
public class LockingManagerImpl implements LockingManager {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public <T> T withLock(String lockString, Supplier<T> codeToExecute) {
		return jdbcTemplate.execute((ConnectionCallback<T>) conn -> {
			PreparedStatement lockStatement = conn.prepareStatement("SELECT pg_try_advisory_lock(?)");
			lockStatement.setLong(1, lockString.hashCode());

			PreparedStatement unlockStatement = conn.prepareStatement("SELECT pg_advisory_unlock(?)");
			unlockStatement.setLong(1, lockString.hashCode());

			try {
				var lockResult = lockStatement.executeQuery();
				lockResult.next();

				log.info("pg_try_advisory_lock result: {}", lockResult.getBoolean("pg_try_advisory_lock"));

				if (!lockResult.getBoolean("pg_try_advisory_lock")) {
					throw new LockCouldNotBeAcquiredException("Could not acquire lock for " + lockString);
				}

				return codeToExecute.get();
			} catch (LockCouldNotBeAcquiredException ex) {
				throw ex;
			} finally {
				var unlockResult = unlockStatement.executeQuery();
				if (unlockResult.next()) {
					log.info("pg_advisory_unlock result: {}", unlockResult.getBoolean(1));
				}
			}
		});
	}

	@Override
	public void withLock(String lockString, Runnable codeToExecute) {
		withLock(lockString, () -> {
			codeToExecute.run();
			return null;
		});
	}
}
