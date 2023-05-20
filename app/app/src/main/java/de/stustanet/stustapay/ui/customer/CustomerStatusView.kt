package de.stustanet.stustapay.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.stustanet.stustapay.model.UserTag
import de.stustanet.stustapay.ui.chipscan.NfcScanDialog
import de.stustanet.stustapay.ui.chipscan.rememberNfcScanDialogState
import de.stustanet.stustapay.ui.nav.NavScaffold
import kotlinx.coroutines.launch
import java.lang.NumberFormatException

@Preview
@Composable
fun CustomerStatusView(
    leaveView: () -> Unit = {},
    viewModel: CustomerStatusViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scanState = rememberNfcScanDialogState()
    var targetTagID by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(scanState) {
        viewModel.startScan()
        scanState.open()
    }

    NavScaffold(title = { Text("Account Status") }, navigateBack = leaveView) {
        Scaffold(
            content = {
                Box(modifier = Modifier.padding(it)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        when (val state = uiState.state) {
                            is CustomerStatusRequestState.Fetching -> {
                                Text("Fetching status...", fontSize = 48.sp)
                            }

                            is CustomerStatusRequestState.Done -> {
                                val customer = state.customer

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("ID", fontSize = 48.sp)
                                    Text(customer.id.toString(), fontSize = 24.sp)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Name", fontSize = 48.sp)
                                    Text(customer.name, fontSize = 24.sp)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Cash", fontSize = 48.sp)
                                    Text("${customer.balance}€", fontSize = 24.sp)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Coupons", fontSize = 48.sp)
                                    Text(customer.vouchers.toString(), fontSize = 24.sp)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Comment", fontSize = 48.sp)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(customer.comment, fontSize = 24.sp)
                                }
                            }

                            is CustomerStatusRequestState.Failed -> {
                                Text("Request failed", fontSize = 48.sp)
                            }

                            is CustomerStatusRequestState.Swap -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Old Tag",
                                        fontSize = 48.sp,
                                        modifier = Modifier.padding(end = 20.dp)
                                    )
                                    TextField(
                                        modifier = Modifier.fillMaxWidth(),
                                        value = targetTagID,
                                        onValueChange = { id -> targetTagID = id },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }

                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    onClick = {
                                        scanState.open()
                                    }
                                ) {
                                    Text("Scan", fontSize = 24.sp)
                                }

                                NfcScanDialog(state = scanState, onScan = { tag ->
                                    targetTagID = tag.uid.toString()
                                })
                            }

                            is CustomerStatusRequestState.SwapDone -> {
                                Text("Swapped tag accounts", fontSize = 48.sp)
                            }
                        }
                    }
                }
            },
            bottomBar = {
                when (val state = uiState.state) {
                    is CustomerStatusRequestState.Done -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth(0.75f)
                                    .padding(end = 10.dp),
                                onClick = {
                                    viewModel.startScan()
                                    scanState.open()
                                }
                            ) {
                                Text("Scan", fontSize = 24.sp)
                            }
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                onClick = {
                                    viewModel.startSwap(UserTag(state.customer.user_tag_uid))
                                }
                            ) {
                                Text("Swap", fontSize = 24.sp)
                            }
                        }

                        NfcScanDialog(
                            state = scanState,
                            onScan = { tag ->
                                scope.launch {
                                    viewModel.completeScan(tag.uid)
                                }
                            }
                        )
                    }

                    is CustomerStatusRequestState.Swap -> {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            onClick = {
                                try {
                                    scope.launch {
                                        try {
                                            viewModel.completeSwap(
                                                targetTagID.toULong(),
                                                state.newTag
                                            )
                                            targetTagID = ""
                                        } catch (_: NumberFormatException) {
                                        }
                                    }
                                } catch (e: NumberFormatException) {
                                    e.printStackTrace()
                                }
                            }
                        ) {
                            Text("Confirm Swap", fontSize = 24.sp)
                        }
                    }

                    else -> {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            onClick = {
                                viewModel.startScan()
                                scanState.open()
                            }
                        ) {
                            Text("Scan", fontSize = 24.sp)
                        }

                        NfcScanDialog(
                            state = scanState,
                            onScan = { tag ->
                                scope.launch {
                                    viewModel.completeScan(tag.uid)
                                }
                            }
                        )
                    }
                }
            }
        )
    }
}
