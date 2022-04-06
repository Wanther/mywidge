package net.wlab.widget.verifyinput

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Selection
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.view.inputmethod.InputConnectionCompat
import net.wlab.widget.R
import kotlin.math.max
import kotlin.math.min

class VerifyInputView : LinearLayout, View.OnClickListener, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {

    companion object {
        private const val TAG = "VerifyInputView"
        private const val PLACEHOLDER = " "
        private const val DEFAULT_ITEM_COUNT = 6
        var DEBUG = false
    }

    private val content: Editable

    private val clipboardManager: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    var verifyInputListener: OnVerifyInputListener? = null

    private var popupMenu: VerifyInputPopup? = null

    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {

        val a = context.obtainStyledAttributes(attrs, R.styleable.VerifyInputView, defStyleAttr, defStyleRes)

        val itemCount = a.getInt(R.styleable.VerifyInputView_itemCount, DEFAULT_ITEM_COUNT)
        content = Editable.Factory.getInstance().newEditable(PLACEHOLDER.repeat(itemCount))
        Selection.setSelection(content, 0, 1)

        isFocusableInTouchMode = true

        val itemBackgrounds = (0 until itemCount).map { a.getDrawable(R.styleable.VerifyInputView_itemBackground) }
        val itemSize = a.getDimensionPixelSize(R.styleable.VerifyInputView_itemSize, 48)
        val itemTextSize = a.getDimension(R.styleable.VerifyInputView_itemTextSize, 36f)
        val itemGap = a.getDimensionPixelSize(R.styleable.VerifyInputView_itemGap, 8)

        (0 until itemCount).forEach { i ->
            addView(createInputItem(i, itemSize, itemTextSize, itemGap, itemBackgrounds[i], itemCount))
        }

        a.recycle()
    }

    /**
     * itemView clicked
     */
    override fun onClick(v: View) {
        if (!isFocused) {
            requestFocus()
        }

        val i = v.tag as Int
        Selection.setSelection(content, i, i + 1)
        updateItemState()
    }

    override fun onLongClick(v: View): Boolean {
        val i = v.tag as Int
        Selection.setSelection(content, i, min(i + 1, childCount))
        updateItemState()

        if (popupMenu == null) {
            popupMenu = VerifyInputPopup(context, this).also {
                it.setOnMenuItemClickListener(this)
            }
        }
        popupMenu!!.show()

        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.paste -> {
                clipboardManager.primaryClip?.getItemAt(0)?.text?.also { clipText ->
                    updateContent(clipText)
                }
                true
            }
            else -> false
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (DEBUG) {
            Log.d(TAG, "keyCode=$keyCode")
        }
        when(keyCode) {
            KeyEvent.KEYCODE_ENTER -> verifyInputListener?.onVerifyInputComplete(content.toString())
            KeyEvent.KEYCODE_DEL -> deleteChar()
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onCheckIsTextEditor(): Boolean = true

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        outAttrs.inputType = EditorInfo.TYPE_CLASS_PHONE
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE
        return InputConnectionCompat.createWrapper(this, VerifyInputConnection(), outAttrs)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        (0 until childCount).forEach { i ->
            getChildAt(i).isEnabled = enabled
        }
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)

        if (DEBUG) {
            Log.d(TAG, "onFocusChanged, gainFocus=$gainFocus")
        }

        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        if (gainFocus) {
            updateItemState()
            inputMethodManager?.showSoftInput(this, 0)
        } else {
            (0 until childCount).forEach { i ->
                getChildAt(i).isSelected = false
            }
            inputMethodManager?.hideSoftInputFromWindow(this.windowToken, 0)
        }
    }

    fun updateContent(text: CharSequence, updateUiState: Boolean = true) {
        if (!isEnabled) {
            return
        }

        val start = Selection.getSelectionStart(content)

        val validText = if (start + text.length > childCount) text.subSequence(0, childCount - start) else text

        content.replace(start, start + validText.length, validText)

        Selection.setSelection(content, min(start + validText.length, childCount - 1), min(start + validText.length + 1, childCount))

        if (updateUiState) {
            updateItemState()
        }

        if (start + validText.length == childCount && validText != PLACEHOLDER) {
            verifyInputListener?.onVerifyInputComplete(content.toString())
        }
    }

    private fun createInputItem(i: Int, itemSize: Int, itemTextSize: Float, itemGap: Int, itemBackground: Drawable?, itemCount: Int): View {
        val itemView = TextView(context)
        itemView.tag = i
        itemView.background = itemBackground
        itemView.layoutParams = LayoutParams(itemSize, itemSize).also { params ->
            params.weight = 1f
            params.setMargins(if (i == 0) 0 else (itemGap / 2f).toInt(), 0, if (i == itemCount - 1) 0 else (itemGap / 2f).toInt(), 0)
        }
        itemView.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize)
        itemView.gravity = Gravity.CENTER
        itemView.isFocusable = false
        itemView.setOnClickListener(this)
        itemView.isLongClickable = true
        itemView.setOnLongClickListener(this)
        return itemView
    }

    private fun deleteChar() {
        if (!isEnabled) {
            return
        }

        val start = Selection.getSelectionStart(content)
        val end = Selection.getSelectionEnd(content)

        val deletedChar = content.substring(start, end)

        if (deletedChar == PLACEHOLDER) {
            Selection.setSelection(content, max(start - 1, 0), max(end - 1, 1))
        } else {
            updateContent(PLACEHOLDER, false)
            Selection.setSelection(content, start, end)
        }

        updateItemState()
    }

    private fun updateItemState() {
        val start = Selection.getSelectionStart(content)
        val end = Selection.getSelectionEnd(content)

        (0 until childCount).forEach { i ->
            val itemView = getChildAt(i) as TextView
            itemView.text = content.substring(i, i + 1)
            itemView.isSelected = i in start until end
        }
    }

    inner class VerifyInputConnection : BaseInputConnection(this, true) {
        override fun getEditable(): Editable = content
        override fun commitText(text: CharSequence, newCursorPosition: Int): Boolean {
            updateContent(text)
            return true
        }
    }

    inner class VerifyInputPopup(context: Context, anchor: View) : PopupMenu(context, anchor) {
        init {
            menu.add(0, android.R.id.paste, 0, android.R.string.paste)
        }

        override fun show() {
            menu.findItem(android.R.id.paste).isEnabled = clipboardManager.hasPrimaryClip()
            super.show()
        }
    }

    interface OnVerifyInputListener {
        fun onVerifyInputComplete(content: String)
    }
}