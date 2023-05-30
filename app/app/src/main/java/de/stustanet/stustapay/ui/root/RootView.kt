package de.stustanet.stustapay.ui.root

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.stustanet.stustapay.ui.cashiermanagement.CashierManagementView
import de.stustanet.stustapay.ui.cashier.CashierStatusView
import de.stustanet.stustapay.ui.debug.DebugView
import de.stustanet.stustapay.ui.history.SaleHistoryView
import de.stustanet.stustapay.ui.nav.NavChangeHandler
import de.stustanet.stustapay.ui.nav.navigateDestination
import de.stustanet.stustapay.ui.sale.SaleView
import de.stustanet.stustapay.ui.settings.SettingsView
import de.stustanet.stustapay.ui.account.AccountView
import de.stustanet.stustapay.ui.ticket.TicketView
import de.stustanet.stustapay.ui.payinout.CashInOutView
import de.stustanet.stustapay.ui.user.UserView
import de.stustanet.stustapay.ui.reward.RewardView
import de.stustanet.stustapay.util.SysUiController


@Composable
fun RootView(uictrl: SysUiController? = null) {
    val navController = rememberNavController()

    if (uictrl != null) {
        navController.addOnDestinationChangedListener(
            NavChangeHandler(RootNavDests, uictrl)
        )
    }

    NavHost(
        navController = navController,
        startDestination = RootNavDests.startpage.route,
    ) {
        composable(RootNavDests.startpage.route) {
            StartpageView(
                navigateTo = { navTo ->
                    navController.navigateDestination(
                        navTo
                    )
                }
            )
        }
        composable(RootNavDests.ticket.route) {
            TicketView(leaveView = { navController.navigateUp() })
        }
        composable(RootNavDests.sale.route) {
            SaleView(leaveView = { navController.navigateUp() })
        }
        composable(RootNavDests.topup.route) {
            CashInOutView(leaveView = { navController.navigateUp() })
        }
        composable(RootNavDests.status.route) {
            AccountView(leaveView = { navController.navigateUp() })
        }
        composable(RootNavDests.user.route) {
            UserView(leaveView = { navController.navigateUp() })
        }
        composable(RootNavDests.settings.route) {
            SettingsView(leaveView = { navController.navigateUp() })
        }
        composable(RootNavDests.development.route) {
            DebugView(leaveView = { navController.navigateUp() })
        }
        composable(RootNavDests.history.route) {
            SaleHistoryView(leaveView = { navController.navigateUp() })
        }
        composable(RootNavDests.rewards.route) {
            RewardView(leaveView = { navController.navigateUp() })
        }
        composable(RootNavDests.cashierManagement.route) {
            CashierManagementView(leaveView = { navController.navigateUp() })
        }
        composable(RootNavDests.cashierStatus.route) {
            CashierStatusView(leaveView = { navController.navigateUp() })
        }
    }
}
