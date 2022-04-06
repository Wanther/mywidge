- # VerifyInputView

  - ## 使用
    ```xml
    <net.wlab.widget.verifyinput.VerifyInputView
        android:id="@+id/verify_code_input"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        android:layout_marginStart="24dp"
        android:layout_marginEnd="24dp"
        app:itemBackground="@drawable/bg_verify_input_item"
        app:itemSize="48dp"
        app:itemTextSize="32sp"
        app:itemCount="6"
        app:itemGap="8dp"/>
    ```

    ```kotlin
        findViewById<VerifyInputView>(R.id.verifyInput).verifyInputListener = object : VerifyInputView.OnVerifyInputListener {
            override fun onVerifyInputComplete(content: String) {
                TODO()
            }
        }
    ```

  - ## 属性
    
    - `app:itemCount` 输入框个数
    - `app:itemSize` 输入框大小
    - `app:itemTextSize` 输入框文本大小
    - `app:itemGap` 输入框间距
    - `app:itemBackground` 输入框背景,例子
      ```xml
      <selector xmlns:android="http://schemas.android.com/apk/res/android">
          <item android:state_enabled="false">
              <shape android:shape="rectangle">
                  <stroke android:color="?attr/colorError" android:width="2dp"/>
                  <corners android:radius="4dp"/>
              </shape>
          </item>
          <item android:state_selected="true">
              <shape android:shape="rectangle">
                <stroke android:color="?attr/colorControlActivated" android:width="2dp"/>
                  <corners android:radius="4dp"/>
              </shape>
          </item>
          <item>
              <shape android:shape="rectangle">
                  <stroke android:color="?attr/colorControlNormal" android:width="2dp"/>
                  <corners android:radius="4dp"/>
              </shape>
          </item>
      </selector>
      ```