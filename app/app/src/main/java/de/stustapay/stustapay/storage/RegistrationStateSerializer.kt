package de.stustapay.stustapay.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.stustapay.stustapay.model.RegistrationState
import de.stustapay.stustapay.proto.RegistrationStateProto
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Singleton

object RegistrationStateSerializer : Serializer<RegistrationState> {
    override val defaultValue: RegistrationState
        get() = RegistrationState.NotRegistered("no registration state")

    override suspend fun readFrom(input: InputStream): RegistrationState {
        return try {
            val regState = RegistrationStateProto.parseFrom(input);
            if (regState.registered) {
                RegistrationState.Registered(
                    regState.authToken,
                    regState.apiEndpoint,
                    "in local storage"
                )
            } else {
                RegistrationState.NotRegistered(
                    "not registered"
                )
            }
        } catch (exception: InvalidProtocolBufferException) {
            RegistrationState.Error("invalid data in local storage")
        }
    }

    override suspend fun writeTo(t: RegistrationState, output: OutputStream) {
        when (t) {
            is RegistrationState.Registered -> {
                val regState = RegistrationStateProto.newBuilder()
                    .setRegistered(true)
                    .setApiEndpoint(t.apiUrl)
                    .setAuthToken(t.token)
                    .build()
                regState.writeTo(output);
            }

            is RegistrationState.NotRegistered -> {
                val regState =
                    RegistrationStateProto.newBuilder().clear().setRegistered(false).build()
                regState.writeTo(output);
            }

            else -> {
                error("Tried to serialize invalid RegistrationState: $t")
            }
        }
    }
}


@InstallIn(SingletonComponent::class)
@Module
class RegistrationStateDataStoreModule {
    @Singleton
    @Provides
    fun createRegistrationStateDataStore(@ApplicationContext context: Context): DataStore<RegistrationState> =
        DataStoreFactory.create(
            serializer = RegistrationStateSerializer,
            produceFile = {
                File("${context.cacheDir.path}/registration_state.pb")
            }
        )
}