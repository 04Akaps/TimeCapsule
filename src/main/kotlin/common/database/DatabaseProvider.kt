package com.example.common.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

enum class TransactionPropagation {
    REQUIRED,       // 트랜잭션 필요, 없으면 생성
    REQUIRES_NEW,   // 항상 새 트랜잭션 생성
    SUPPORTS        // 트랜잭션 있으면 사용, 없어도 진행
}

object DatabaseProvider {
    suspend fun <T> dbQuery(
        propagation: TransactionPropagation = TransactionPropagation.REQUIRED,
        readOnly: Boolean = false,
        block: suspend () -> T
    ): T {
        val current = TransactionManager.currentOrNull()

        return when (propagation) {
            TransactionPropagation.REQUIRED -> {
                if (current != null && !current.connection.isClosed) {
                    block()
                } else {
                    newSuspendedTransaction(Dispatchers.IO) {
                        configureReadOnly(readOnly) {
                            block()
                        }
                    }
                }
            }
            TransactionPropagation.REQUIRES_NEW -> {
                newSuspendedTransaction(Dispatchers.IO) {
                    configureReadOnly(readOnly) {
                        block()
                    }
                }
            }
            TransactionPropagation.SUPPORTS -> block()
        }
    }

    private suspend fun <T> Transaction.configureReadOnly(readOnly: Boolean, block: suspend () -> T): T {
        if (readOnly) {
            connection.readOnly = true
        }

        return block()
    }
}