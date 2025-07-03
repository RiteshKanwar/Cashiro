package com.ritesh.cashiro.presentation.ui.extras.icons

import androidx.compose.ui.graphics.vector.ImageVector
import com.ritesh.cashiro.presentation.ui.extras.icons.myiconpack.Calendar
import com.ritesh.cashiro.presentation.ui.extras.icons.myiconpack.Expense
import com.ritesh.cashiro.presentation.ui.extras.icons.myiconpack.ExpenseFilled
import com.ritesh.cashiro.presentation.ui.extras.icons.myiconpack.More
import com.ritesh.cashiro.presentation.ui.extras.icons.myiconpack.MoreFilled
import com.ritesh.cashiro.presentation.ui.extras.icons.myiconpack.Reports
import com.ritesh.cashiro.presentation.ui.extras.icons.myiconpack.ReportsFilled
import com.ritesh.cashiro.presentation.ui.extras.icons.myiconpack.Transaction
import com.ritesh.cashiro.presentation.ui.extras.icons.myiconpack.TransactionFilled

import kotlin.collections.List as ____KtList

public object MyIconPack

private var __AllIcons: ____KtList<ImageVector>? = null

public val MyIconPack.AllIcons: ____KtList<ImageVector>
  get() {
    if (__AllIcons != null) {
      return __AllIcons!!
    }
    __AllIcons= listOf(
      Calendar, Expense, ExpenseFilled, More, MoreFilled, Reports, ReportsFilled,
      Transaction, TransactionFilled
    )
    return __AllIcons!!
  }
