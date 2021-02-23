/*
 * Copyright 2021 Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * File modified by Appmattus Limited
 * See: https://github.com/appmattus/certificatetransparency/compare/e3d469df9be35bcbf0f564d32ca74af4e5ca4ae5...main
 */

package com.babylon.certificatetransparency.sampleapp.examples

import android.app.Application
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.babylon.certificatetransparency.sampleapp.R
import com.babylon.certificatetransparency.sampleapp.SpaceItemDecoration
import com.babylon.certificatetransparency.sampleapp.databinding.ExampleFragmentBinding
import com.babylon.certificatetransparency.sampleapp.item.CheckboxItem
import com.babylon.certificatetransparency.sampleapp.item.CodeViewItem
import com.babylon.certificatetransparency.sampleapp.item.RemovableItem
import com.babylon.certificatetransparency.sampleapp.item.button.ButtonItem
import com.babylon.certificatetransparency.sampleapp.item.button.OutlinedButtonItem
import com.babylon.certificatetransparency.sampleapp.item.text.BodyTextItem
import com.babylon.certificatetransparency.sampleapp.item.text.HeaderTextItem
import com.babylon.certificatetransparency.sampleapp.item.text.SubHeaderTextItem
import com.babylon.certificatetransparency.sampleapp.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section

abstract class BaseExampleFragment<T : BaseExampleViewModel> : Fragment(R.layout.example_fragment) {

    private val binding by viewBinding<ExampleFragmentBinding>()

    abstract fun getViewModelClass(): Class<T>

    private lateinit var viewModel: T

    private val codeViewItem = CodeViewItem()

    private val hostsSection = Section().apply {
        setHeader(
            BodyTextItem(
                title = "Verify certificate transparency for hosts that match one of the patterns."
            )
        )

        setFooter(
            OutlinedButtonItem(
                R.string.include_host,
                iconResId = R.drawable.plus
            ) {
                showIncludeHostDialog()
            }
        )
    }

    private val failOnErrorSection = Section().apply {
        setHeader(
            BodyTextItem(
                title = "Determine if a failure to pass certificate transparency results in the connection being closed. " +
                        "A value of true ensures the connection is closed on errors.\nDefault: true"
            )
        )
    }

    private var snackbar: Snackbar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.example_fragment, container, false)
    }

    @Suppress("LongMethod")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val appContext = requireContext().applicationContext as Application
        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(appContext).create(getViewModelClass())

        viewModel.liveData.observe(
            viewLifecycleOwner,
            { state ->
                updateHosts(state)
                updateMessage(state)
                updateCode(state)
                updateFailOnError(state)
            }
        )

        binding.hostsRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.hostsRecyclerView.setHasFixedSize(false)

        @Suppress("MagicNumber")
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics)
        binding.hostsRecyclerView.addItemDecoration(SpaceItemDecoration(px.toInt()))

        (binding.hostsRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        binding.hostsRecyclerView.adapter = GroupAdapter<GroupieViewHolder>().apply {
            add(HeaderTextItem(getTitle()))
            add(SubHeaderTextItem(R.string.configuration))
            add(hostsSection)
            add(failOnErrorSection)
            add(SubHeaderTextItem(R.string.sample_code))
            add(codeViewItem)
            add(
                ButtonItem(R.string.test_certificate_transparency) {
                    showConnectionDialog()
                }
            )
        }
    }

    @StringRes
    abstract fun getTitle(): Int

    private fun updateFailOnError(state: State) {
        failOnErrorSection.update(
            listOf(
                CheckboxItem(
                    getString(R.string.fail_on_error),
                    state.failOnError
                ) {
                    viewModel.setFailOnError(it)
                }
            )
        )
    }

    private fun updateCode(state: State) {
        codeViewItem.source = state.sampleCode
    }

    private fun updateMessage(state: State) {
        snackbar = if (state.message != null) {
            snackbar?.dismiss()
            Snackbar.make(this@BaseExampleFragment.view!!, state.message.text, Snackbar.LENGTH_LONG).apply {
                val color = when (state.message) {
                    is State.Message.Success -> R.color.colorSuccess
                    is State.Message.Failure -> R.color.colorFailure
                }

                view.setBackgroundColor(ContextCompat.getColor(context, color))

                addCallback(
                    object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                            if (snackbar == this@apply) {
                                viewModel.dismissMessage()
                            }
                        }
                    }
                )
                show()
            }
        } else {
            snackbar?.dismiss()
            null
        }
    }

    private fun updateHosts(state: State) {
        hostsSection.update(
            state.hosts.map { host ->
                RemovableItem(host) {
                    viewModel.removeHost(it.title.toString())
                }
            }
        )
    }

    private fun showIncludeHostDialog() {
        MaterialDialog(requireContext()).show {
            title(R.string.include_host_title)
            message(R.string.include_host_message)
            input { _, text ->
                viewModel.includeHost(text.toString())
            }

            positiveButton(text = "Include")
            negativeButton(text = "Cancel")
        }
    }

    private fun showConnectionDialog() {
        MaterialDialog(requireContext()).show {
            title(text = "Test connection")
            message(text = "Please provide a host to test a connection to. 'https://' will be automatically added")
            input { _, text ->
                viewModel.openConnection(text.toString())
            }

            positiveButton(text = "Connect")
            negativeButton(text = "Cancel")
        }
    }
}
