package com.seuprojeto.safeshot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var imageViewOriginal: ImageView
    private lateinit var imageViewClean: ImageView
    private lateinit var btnCamera: Button
    private lateinit var btnGallery: Button
    private lateinit var btnClean: Button
    private lateinit var btnShare: Button
    private lateinit var tvOriginalMeta: TextView
    private lateinit var tvCleanMeta: TextView
    private lateinit var switchAutoClean: Switch
    private lateinit var switchAutoCleanCamera: Switch

    private var currentPhotoPath: String? = null
    private var cleanPhotoPath: String? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            if (switchAutoClean.isChecked) {
                autoCleanAfterCamera()
            } else {
                showOriginalImage()
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val path = getRealPathFromURI(it)
            currentPhotoPath = path
            showOriginalImage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageViewOriginal = findViewById(R.id.imageViewOriginal)
        imageViewClean = findViewById(R.id.imageViewClean)
        btnCamera = findViewById(R.id.btnCamera)
        btnGallery = findViewById(R.id.btnGallery)
        btnClean = findViewById(R.id.btnClean)
        btnShare = findViewById(R.id.btnShare)
        tvOriginalMeta = findViewById(R.id.tvOriginalMeta)
        tvCleanMeta = findViewById(R.id.tvCleanMeta)
        switchAutoClean = findViewById(R.id.switchAutoClean)
        switchAutoCleanCamera = findViewById(R.id.switchAutoCleanCamera)

        btnCamera.setOnClickListener { openCamera() }
        btnGallery.setOnClickListener { openGallery() }
        btnClean.setOnClickListener { removeMetadata() }
        btnShare.setOnClickListener { shareCleanPhoto() }

        val prefs = getSharedPreferences("safeshot_prefs", MODE_PRIVATE)
        switchAutoCleanCamera.isChecked = prefs.getBoolean("auto_clean_camera", false)
        switchAutoCleanCamera.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_clean_camera", isChecked).apply()
        }

        checkPermissions()

        // Verifica se o app foi aberto via compartilhamento
        handleShareIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleShareIntent(it) }
    }

    private fun handleShareIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            imageUri?.let {
                val path = getRealPathFromURI(it)
                currentPhotoPath = path
                showOriginalImage()
                // Remove metadados automaticamente ao receber compartilhamento
                removeMetadata()
                Toast.makeText(this, "Metadados removidos! Agora você pode compartilhar a imagem limpa.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        }
        val toRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (toRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, toRequest.toTypedArray(), 1)
        }
    }

    private fun openCamera() {
        val file = File.createTempFile("safeshot_", ".jpg", cacheDir)
        currentPhotoPath = file.absolutePath
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        cameraLauncher.launch(uri)
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun showOriginalImage() {
        currentPhotoPath?.let {
            Glide.with(this).load(it).into(imageViewOriginal)
            val exif = ExifInterface(it)
            tvOriginalMeta.text = MetadataUtils.getMetadataString(exif)
        }
    }

    private fun removeMetadata() {
        currentPhotoPath?.let { path ->
            val original = File(path)
            val cleanFile = File.createTempFile("safeshot_clean_", ".jpg", cacheDir)
            cleanPhotoPath = cleanFile.absolutePath

            // Copia a imagem original para o novo arquivo
            original.copyTo(cleanFile, overwrite = true)

            // Remove metadados
            val exif = ExifInterface(cleanFile)
            MetadataUtils.cleanMetadata(exif)

            Glide.with(this).load(cleanFile).into(imageViewClean)
            tvCleanMeta.text = MetadataUtils.getMetadataString(exif)
            Toast.makeText(this, "Metadados removidos!", Toast.LENGTH_SHORT).show()
            logCleanAction(cleanFile.absolutePath)
        }
    }

    private fun autoCleanAfterCamera() {
        currentPhotoPath?.let { path ->
            val original = File(path)
            val cleanFile = File.createTempFile("safeshot_clean_", ".jpg", cacheDir)
            cleanPhotoPath = cleanFile.absolutePath

            // Copia a imagem original para o novo arquivo
            original.copyTo(cleanFile, overwrite = true)

            // Remove metadados
            val exif = ExifInterface(cleanFile)
            MetadataUtils.cleanMetadata(exif)

            // Exibe imagem limpa e metadados limpos
            Glide.with(this).load(cleanFile).into(imageViewClean)
            tvCleanMeta.text = MetadataUtils.getMetadataString(exif)
            // Limpa a exibição da original para reforçar a segurança
            imageViewOriginal.setImageDrawable(null)
            tvOriginalMeta.text = "Metadados Originais: (removidos automaticamente)"
            Toast.makeText(this, "Metadados removidos automaticamente!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareCleanPhoto() {
        cleanPhotoPath?.let {
            val file = File(it)
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Compartilhar imagem limpa"))
        }
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        var path: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = cursor.getString(columnIndex)
            }
        }
        return path
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_log -> {
                startActivity(Intent(this, LogActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logCleanAction(filePath: String?) {
        filePath ?: return
        val prefs = getSharedPreferences("safeshot_log", MODE_PRIVATE)
        val logs = prefs.getStringSet("log_entries", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val entry = "[${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(java.util.Date())}] Limpeza: ${filePath.substringAfterLast('/')}"
        logs.add(entry)
        prefs.edit().putStringSet("log_entries", logs).apply()
    }
} 