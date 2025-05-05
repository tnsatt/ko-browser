/*Copyright by MonnyLab*/

package com.xlab.vbrowser.autocomplete

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
//import kotlinx.android.synthetic.main.fragment_autocomplete_customdomains.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import com.xlab.vbrowser.R
import com.xlab.vbrowser.settings.SettingsFragment
import kotlinx.coroutines.CoroutineScope

class AutocompleteCustomDomainsFragment : Fragment() {
    lateinit var domainList: RecyclerView
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater!!.inflate(R.layout.fragment_autocomplete_customdomains, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        domainList = view.findViewById(R.id.domainList)
        domainList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        domainList.adapter = DomainListAdapter()
    }

    override fun onResume() {
        super.onResume()

        val updater = activity as SettingsFragment.ActionBarUpdater
        updater.updateTitle(R.string.preference_autocomplete_subitem_customlist)
        updater.updateIcon(R.drawable.ic_back)

        (domainList.adapter as DomainListAdapter).refresh(activity)
    }

    private inner class DomainListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val domains: MutableList<String> = mutableListOf()

        fun refresh(context: Context) {
            CoroutineScope(Dispatchers.Main).launch {
                val updatedDomains = async { CustomAutoComplete.loadCustomAutoCompleteDomains(context) }.await()

                domains.clear()
                domains.addAll(updatedDomains)

                notifyDataSetChanged()
            }
        }

        override fun getItemViewType(position: Int) =
                when (position) {
                    domains.size -> AddActionViewHolder.LAYOUT_ID
                    else -> DomainViewHolder.LAYOUT_ID
                }

        override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
                when (viewType) {
                    AddActionViewHolder.LAYOUT_ID ->
                        AddActionViewHolder(
                                this@AutocompleteCustomDomainsFragment,
                                LayoutInflater.from(parent.context).inflate(viewType, parent, false))
                    DomainViewHolder.LAYOUT_ID ->
                        DomainViewHolder(
                                LayoutInflater.from(parent.context).inflate(viewType, parent, false))
                    else -> throw IllegalArgumentException("Unknown view type: $viewType")
                }

        override fun getItemCount(): Int = domains.size + 1

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is DomainViewHolder) {
                holder.bind(domains[position])
            }
        }
    }

    private class DomainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            val LAYOUT_ID = R.layout.item_custom_domain
        }

        fun bind(domain: String) {
            (itemView as TextView).text  = domain
        }
    }

    private class AddActionViewHolder(val fragment: AutocompleteCustomDomainsFragment, itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                fragment.fragmentManager
                        .beginTransaction()
                        .replace(R.id.container, AutocompleteAddDomainFragment())
                        .addToBackStack(null)
                        .commit()
            }
        }

        companion object {
            val LAYOUT_ID = R.layout.item_add_custom_domain
        }
    }
}