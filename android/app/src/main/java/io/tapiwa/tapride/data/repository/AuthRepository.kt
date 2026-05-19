package io.tapiwa.tapride.data.repository

import io.appwrite.services.Account
import io.appwrite.models.User
import io.tapiwa.tapride.util.TapLogger

/**
 * Handles all Appwrite authentication operations.
 *
 * Every public method returns [Result] so callers can handle success and
 * failure without try/catch at the call site.
 *
 * @param account The Appwrite [Account] service instance.
 */
class AuthRepository(private val account: Account) {

    /**
     * Registers a new user with email and password.
     *
     * @param email    User's email address.
     * @param password Password (minimum 8 characters).
     * @param name     Display name shown on the profile.
     * @return [Result] wrapping the created [User] on success.
     */
    suspend fun register(
        email: String,
        password: String,
        name: String
    ): Result<User<Map<String, Any>>> = try {
        val user = account.create(
            userId = io.appwrite.ID.unique(),
            email = email,
            password = password,
            name = name
        )
        TapLogger.d("AuthRepository", "Registered user: ${user.id}")
        Result.success(user)
    } catch (e: Exception) {
        TapLogger.e("AuthRepository", "register failed", e)
        Result.failure(e)
    }

    /**
     * Logs in an existing user with email and password.
     * Creates a session stored by the Appwrite SDK.
     *
     * @param email    Registered email address.
     * @param password Account password.
     * @return [Result] wrapping the created session on success.
     */
    suspend fun login(email: String, password: String): Result<io.appwrite.models.Session> = try {
        val session = account.createEmailPasswordSession(email, password)
        TapLogger.d("AuthRepository", "Logged in session: ${session.id}")
        Result.success(session)
    } catch (e: Exception) {
        TapLogger.e("AuthRepository", "login failed", e)
        Result.failure(e)
    }

    /**
     * Deletes the current active session, effectively logging the user out.
     *
     * @return [Result] of [Unit] indicating success or failure.
     */
    suspend fun logout(): Result<Unit> = try {
        account.deleteSession("current")
        TapLogger.d("AuthRepository", "Logged out")
        Result.success(Unit)
    } catch (e: Exception) {
        TapLogger.e("AuthRepository", "logout failed", e)
        Result.failure(e)
    }

    /**
     * Retrieves the currently authenticated user.
     *
     * @return [Result] wrapping the [User] if a session exists, or failure if not logged in.
     */
    suspend fun currentUser(): Result<User<Map<String, Any>>> = try {
        val user = account.get()
        TapLogger.d("AuthRepository", "Current user: ${user.id}")
        Result.success(user)
    } catch (e: Exception) {
        TapLogger.d("AuthRepository", "No active session")
        Result.failure(e)
    }
}
