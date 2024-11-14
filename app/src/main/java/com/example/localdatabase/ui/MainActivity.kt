package com.example.localdatabase.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.localdatabase.R
import com.example.localdatabase.database.Note
import com.example.localdatabase.database.NoteDao
import com.example.localdatabase.database.NoteRoomDatabase
import com.example.localdatabase.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteDao: NoteDao
    private lateinit var executorService: ExecutorService
    private var updateId: Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        executorService = Executors.newSingleThreadExecutor()
        val db = NoteRoomDatabase.getDatabase(this)
        noteDao = db!!.noteDao()!!


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            btnAdd.setOnClickListener(View.OnClickListener {
                insert(
                    Note(
                        title = edtTitle.text.toString(),
                        description = edtDesc.text.toString(),
                        date = edtDate.text.toString()
                    )
                )
                setEmptyField()
            })
            btnUpdate.setOnClickListener{
                update(
                    Note(
                        id = updateId,
                        title = edtTitle.text.toString(),
                        description = edtDesc.text.toString(),
                        date = edtDate.text.toString()
                    )
                )
                updateId = 0
                setEmptyField()
            }
            listView.setOnItemClickListener { adapterView, view, i, l ->
                val item = adapterView.adapter.getItem(i) as Note
                updateId = item.id
                edtTitle.setText(item.title)
                edtDesc.setText(item.description)
                edtDate.setText(item.date)
            }
            listView.onItemLongClickListener =
                AdapterView.OnItemLongClickListener { adapterView, view, i, l ->
                    val item = adapterView.adapter.getItem(i) as Note
                    delete(item)
                    true
                }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        getAllNotes()
    }

    fun setEmptyField() {
        with(binding) {
            edtTitle.setText("")
            edtDesc.setText("")
            edtDate.setText("")
        }
    }

    fun getAllNotes(){
        noteDao.allNotes.observe(this) {
                notes ->
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
                notes)
            binding.listView.adapter = adapter
            }
        }

    fun insert(note : Note){
        executorService.execute {
            noteDao.insert(note)
        }
    }

    fun delete(note: Note) {
        executorService.execute {
            noteDao.delete(note)
        }
    }

    fun update(note: Note) {
        executorService.execute {
            noteDao.update(note)
        }
    }
}