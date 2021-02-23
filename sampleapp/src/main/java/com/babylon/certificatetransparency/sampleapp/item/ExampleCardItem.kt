/*
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
 */

package com.babylon.certificatetransparency.sampleapp.item

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.babylon.certificatetransparency.sampleapp.R
import com.google.android.material.snackbar.Snackbar
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.example_card_item.view.*

class ExampleCardItem(
    private val navController: NavController,
    private val title: String,
    private val uri: Uri,
    private val kotlinNav: Int,
    private val javaNav: Int
) : Item() {

    override fun getLayout() = R.layout.example_card_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.containerView.title.text = title

        viewHolder.containerView.link.setOnClickListener {
            try {
                val myIntent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(viewHolder.containerView.context, myIntent, Bundle())
            } catch (ignored: ActivityNotFoundException) {
                Snackbar.make(viewHolder.containerView, "Unable to open external link", Snackbar.LENGTH_SHORT).show()
            }
        }

        viewHolder.containerView.kotlin.setOnClickListener {
            navController.navigate(kotlinNav)
        }

        viewHolder.containerView.java.setOnClickListener {
            navController.navigate(javaNav)
        }
    }
}
