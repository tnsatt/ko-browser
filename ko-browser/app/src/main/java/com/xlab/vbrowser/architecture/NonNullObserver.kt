/*Copyright by MonnyLab*/

package com.xlab.vbrowser.architecture

import androidx.lifecycle.Observer

abstract class NonNullObserver<T> : Observer<T> {
    protected abstract fun onValueChanged(t: T)

    override fun onChanged(value: T?) {
        value?.let {
            onValueChanged(it)
        }
    }
}
