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

package com.appmattus.certificatetransparency.sampleapp

import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appmattus.certificatetransparency.sampleapp.databinding.FragmentMainBinding
import com.appmattus.certificatetransparency.sampleapp.item.BabylonLogoItem
import com.appmattus.certificatetransparency.sampleapp.item.ExampleCardItem
import com.appmattus.certificatetransparency.sampleapp.item.text.HeaderTextItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

class MainFragment : Fragment(R.layout.fragment_main) {

    private val binding by viewBinding<FragmentMainBinding>()

    @Suppress("LongMethod")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        @Suppress("MagicNumber")
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics)
        binding.recyclerView.addItemDecoration(SpaceItemDecoration(px.toInt()))

        binding.recyclerView.adapter = GroupAdapter<GroupieViewHolder>().apply {
            val navController = findNavController()

            add(HeaderTextItem(R.string.certificate_transparency, iconResId = R.drawable.ic_launcher_foreground))

            add(
                ExampleCardItem(
                    navController,
                    "OkHttp",
                    Uri.parse("https://square.github.io/okhttp/"),
                    R.id.okhttp_kotlin_example_fragment,
                    R.id.okhttp_java_example_fragment
                )
            )
            add(
                ExampleCardItem(
                    navController,
                    "HttpURLConnection",
                    Uri.parse("https://developer.android.com/reference/java/net/HttpURLConnection"),
                    R.id.httpurlconnection_kotlin_example_fragment,
                    R.id.httpurlconnection_java_example_fragment
                )
            )
            add(
                ExampleCardItem(
                    navController,
                    "Volley",
                    Uri.parse("https://developer.android.com/training/volley/index.html"),
                    R.id.volley_kotlin_example_fragment,
                    R.id.volley_java_example_fragment
                )
            )

            add(BabylonLogoItem)
        }
    }
}
