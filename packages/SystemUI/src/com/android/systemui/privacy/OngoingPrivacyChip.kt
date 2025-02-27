/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.systemui.privacy

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.android.systemui.R

class OngoingPrivacyChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttrs: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttrs, defStyleRes) {

    private val iconMarginExpanded = context.resources.getDimensionPixelSize(
                    R.dimen.ongoing_appops_chip_icon_margin_expanded)
    private val iconMarginCollapsed = context.resources.getDimensionPixelSize(
                    R.dimen.ongoing_appops_chip_icon_margin_collapsed)
    private val iconSize =
            context.resources.getDimensionPixelSize(R.dimen.ongoing_appops_chip_icon_size)
    private val iconColor = context.resources.getColor(
            R.color.status_bar_clock_color, context.theme)
    private lateinit var iconsContainer: LinearLayout
    var expanded = false
        set(value) {
            if (value != field) {
                field = value
                updateView()
            }
        }

    var builder = PrivacyDialogBuilder(context, emptyList<PrivacyItem>())
    var privacyList = emptyList<PrivacyItem>()
        set(value) {
            field = value
            builder = PrivacyDialogBuilder(context, value)
            updateView()
        }

    override fun onFinishInflate() {
        super.onFinishInflate()

        iconsContainer = findViewById(R.id.icons_container)
    }

    // Should only be called if the builder icons or app changed
    private fun updateView() {
        fun setIcons(dialogBuilder: PrivacyDialogBuilder, iconsContainer: ViewGroup) {
            iconsContainer.removeAllViews()
            dialogBuilder.generateIcons().forEachIndexed { i, it ->
                it.mutate()
                it.setTint(iconColor)
                val image = ImageView(context).apply {
                    setImageDrawable(it)
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                }
                iconsContainer.addView(image, iconSize, iconSize)
                if (i != 0) {
                    val lp = image.layoutParams as MarginLayoutParams
                    lp.marginStart = if (expanded) iconMarginExpanded else iconMarginCollapsed
                    image.layoutParams = lp
                }
            }
        }

        if (!privacyList.isEmpty()) {
            generateContentDescription()
            setIcons(builder, iconsContainer)
            val lp = iconsContainer.layoutParams as LinearLayout.LayoutParams
            lp.gravity = Gravity.CENTER_VERTICAL or
                    (if (expanded) Gravity.CENTER_HORIZONTAL else Gravity.END)
            iconsContainer.layoutParams = lp
        } else {
            iconsContainer.removeAllViews()
        }
        requestLayout()
    }

    private fun generateContentDescription() {
        val typesText = builder.joinTypes()
        contentDescription = context.getString(
                R.string.ongoing_privacy_chip_content_multiple_apps, typesText)
    }
}