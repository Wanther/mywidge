package cn.beiye.myapplication2.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import cn.beiye.myapplication2.BuildConfig
import cn.beiye.myapplication2.databinding.FragmentDashboardBinding
import net.wlab.widget.verifyinput.VerifyInputView

class DashboardFragment : Fragment(), VerifyInputView.OnVerifyInputListener {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        VerifyInputView.DEBUG = true

        val dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.verifyCodeInput.verifyInputListener = object : VerifyInputView.OnVerifyInputListener {
            override fun onVerifyInputComplete(content: String) {

            }

        }
        binding.test.setOnClickListener {
            binding.verifyCodeInput.isEnabled = !binding.verifyCodeInput.isEnabled
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onVerifyInputComplete(content: String) {
        if (BuildConfig.DEBUG) {
            Log.d("AAA", "onVerifyInputComplete, content=$content")
            binding.verifyCodeInput.isEnabled = false
        }
    }
}