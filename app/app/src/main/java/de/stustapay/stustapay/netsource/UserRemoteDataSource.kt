package de.stustapay.stustapay.netsource


import de.stustapay.stustapay.model.*
import de.stustapay.stustapay.net.Response
import de.stustapay.stustapay.net.TerminalAPI
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(
    private val terminalAPI: TerminalAPI
) {
    suspend fun currentUser(): UserState {
        return when (val userResponse = terminalAPI.currentUser()) {
            is Response.OK -> {
                val user = userResponse.data
                if (user != null) {
                    UserState.LoggedIn(
                        user = user,
                    )
                } else {
                    UserState.NoLogin
                }
            }
            is Response.Error -> {
                UserState.Error(
                    msg = userResponse.msg(),
                )
            }
        }
    }

    /**
     * Login a user by token and desired role.
     */
    suspend fun checkLogin(tag: UserTag): UserRolesState {
        return when (val checkLoginResponse = terminalAPI.checkLogin(tag)) {
            is Response.OK -> {
                UserRolesState.OK(
                    roles = checkLoginResponse.data.roles,
                    tag = checkLoginResponse.data.user_tag,
                )
            }
            is Response.Error -> {
                UserRolesState.Error(
                    msg = checkLoginResponse.msg(),
                )
            }
        }
    }

    /**
     * Login a user by token and desired role.
     */
    suspend fun userLogin(loginPayload: LoginPayload): UserState {
        return when (val userLoginResponse = terminalAPI.userLogin(loginPayload)) {
            is Response.OK -> {
                UserState.LoggedIn(
                    user = userLoginResponse.data,
                )
            }
            is Response.Error -> {
                UserState.Error(
                    msg = userLoginResponse.msg(),
                )
            }
        }
    }

    /**
     * Logout the current user.
     */
    suspend fun userLogout(): String? {
        return when (val userLogoutResponse = terminalAPI.userLogout()) {
            is Response.OK -> {
                null
            }
            is Response.Error -> {
                userLogoutResponse.msg()
            }
        }
    }

    /**
     * Create a new user of any type.
     */
    suspend fun userCreate(newUser: NewUser): UserCreateState {
        return when (val res = terminalAPI.userCreate(newUser)) {
            is Response.OK -> {
                UserCreateState.Created
            }
            is Response.Error -> {
                UserCreateState.Error(res.msg())
            }
        }
    }

    /**
     * Change a user's roles.
     */
    suspend fun userUpdate(updateUser: UpdateUser): UserUpdateState {
        return when (val res = terminalAPI.userUpdate(updateUser)) {
            is Response.OK -> {
                UserUpdateState.Created
            }
            is Response.Error -> {
                UserUpdateState.Error(res.msg())
            }
        }
    }
}