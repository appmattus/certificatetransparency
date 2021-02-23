/*
 * Copyright 2021 Appmattus Limited
 * Copyright 2019 Babylon Partners Limited
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

package com.babylon.certificatetransparency.sampleapp.item

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.babylon.certificatetransparency.sampleapp.R
import com.babylon.certificatetransparency.sampleapp.databinding.ExampleCardItemBinding
import com.google.android.material.snackbar.Snackbar
import com.xwray.groupie.viewbinding.BindableItem

class ExampleCardItem(
    private val navController: NavController,
    private val title: String,
    private val uri: Uri,
    private val kotlinNav: Int,
    private val javaNav: Int
) : BindableItem<ExampleCardItemBinding>() {

    override fun initializeViewBinding(view: View) = ExampleCardItemBinding.bind(view)

    override fun getLayout() = R.layout.example_card_item

    override fun bind(viewBinding: ExampleCardItemBinding, position: Int) {
        val context = viewBinding.root.context

        viewBinding.title.text = title

        viewBinding.link.setOnClickListener {
            try {
                val myIntent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(context, myIntent, Bundle())
            } catch (ignored: ActivityNotFoundException) {
                Snackbar.make(viewBinding.root, "Unable to open external link", Snackbar.LENGTH_SHORT).show()
            }
        }

        viewBinding.kotlin.setOnClickListener {
            navController.navigate(kotlinNav)
        }

        viewBinding.java.setOnClickListener {
            navController.navigate(javaNav)
        }
    }
}
