package com.example.loaddata

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loaddata.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private lateinit var myAdapter: FileAdapter
    private var isRunning = false
    private var scannedFiles = 0
    private lateinit var listFiles : MutableList<Data>
    private val STORAGE_PERMISSION_CODE = 100
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun checkManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Permission already granted!", Toast.LENGTH_SHORT).show()
            }
        }
        else{
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Yêu cầu quyền
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    STORAGE_PERMISSION_CODE
                )
            } else {
                Toast.makeText(this, "Permission already granted!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val statFs = StatFs(Environment.getExternalStorageDirectory().absolutePath)

        // Dung lượng tổng cộng (bytes)
        val totalBytes = statFs.totalBytes


        val availableBytes = statFs.availableBytes

        // Dung lượng đã sử dụng
        val usedBytes = totalBytes - availableBytes

        // Chuyển đổi đơn vị từ bytes sang GB
        val totalGB = totalBytes / (1024.0 * 1024.0 * 1024.0)
        val resTotal = "%.2f".format(totalGB)
        val availableGB = availableBytes / (1024.0 * 1024.0 * 1024.0)
        val resAvaiable = "%.2f".format(availableGB)
        binding.tvFreeSpace.text = "Free Space: $resAvaiable GB"
        binding.tvSpaceStorage.text = "Space Storage: $resTotal GB"
        checkManageExternalStoragePermission()
        binding.btRun.setOnClickListener(){
            startScan()
        }
        listFiles = mutableListOf()
        myAdapter = FileAdapter(listFiles)
        binding.rcvItem.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = myAdapter
            addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
        }
        Log.e("Tag", listFiles.size.toString())
        for (data in listFiles){
            Log.e("Tag", data.toString())
        }
        scanFiles(Environment.getExternalStorageDirectory())
    }
    private fun startScan() {
        if(isRunning){
            isRunning = false
            binding.btRun.text = "Run"
        }
        else{
            binding.btRun.text = "Stop"
            isRunning = true

        }
    }
    @SuppressLint("SuspiciousIndentation")
    private fun scanFiles(files: File) {
        val temp : MutableList<Data> = mutableListOf()
        coroutineScope.launch {
            if (files.exists() && files.isDirectory) {
                val directoriesToScan = mutableListOf(files) // Danh sách chứa các thư mục cần quét
                // Duyệt qua danh sách thư mục để quét
                while (directoriesToScan.isNotEmpty()) {
                    val currentDirectory = directoriesToScan.removeAt(0) // Lấy thư mục đầu tiên trong danh sách
                        currentDirectory.listFiles()?.forEach { file ->
                            if (file.isDirectory) {
                                // Nếu là thư mục, thêm vào danh sách để quét sau
                                directoriesToScan.add(file)
                            } else {
                                // Nếu là tệp, xử lý nó
                                check()
                                val data = Data(file.name, file.path, (file.length()/ (1024.0)).toString(), file.extension)
                                Log.e("Tag", file.toString())
                                temp.add(0, data) // Thêm tệp vào danh sách
                                withContext(Dispatchers.Main) {
                                    delay(1000)
                                    myAdapter.updateData(data)
                                    binding.rcvItem.scrollToPosition(0)
                                    scannedFiles++
                                    binding.tvScannedFiles.text = "Scanned Files: $scannedFiles"
                                }
                            }
                        }
                }
            }
        }
    }
    private suspend fun check()
    {
        while(!isRunning)
        {
            delay(1000)
        }
    }
}

