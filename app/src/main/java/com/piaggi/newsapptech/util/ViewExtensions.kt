package com.piaggi.newsapptech.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.hideKeyboard() {
    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    view?.windowToken?.let { imm.hideSoftInputFromWindow(it, 0) }
}

fun Fragment.showToast(message: String?, duration: Int = Toast.LENGTH_SHORT) {
    message?.let {
        Toast.makeText(requireContext(), it, duration).show()
    }
}

fun View.setVisibleIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}
