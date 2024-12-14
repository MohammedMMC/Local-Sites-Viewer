package tr.com.mohamed.localsitesviewer

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HostsAdapter (private val hosts: List<String>) :
    RecyclerView.Adapter<HostsAdapter.HostViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HostViewHolder {
        return HostViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
        );
    }

    override fun onBindViewHolder(holder: HostViewHolder, position: Int) {
        val host = hosts[position];
        holder.textView.text = host;
        holder.itemView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(host))
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = hosts.size;

    class HostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1);
    }
}