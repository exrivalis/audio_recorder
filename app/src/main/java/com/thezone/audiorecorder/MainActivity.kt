package com.thezone.audiorecorder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.*

const val REQUEST_CODE = 200

class MainActivity : AppCompatActivity(), Timer.OnTimerTickListener{

    private lateinit var amplitudes: ArrayList<Float>
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false

    private lateinit var recorder: MediaRecorder
    private var dirPath = ""
    private var filename = ""
    private var isRecording = false
    private var isPaused = false

    private var duration = ""

    private lateinit var vibrator: Vibrator

    private lateinit var timer: Timer

    private lateinit var db : AppDatabase

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionGranted = ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED

        if(!permissionGranted)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)

        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "audioRecords"
        ).build()

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        timer = Timer(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        btnRecord.setOnClickListener {
            when{
                isPaused -> resumeRecorder()
                isRecording -> pauseRecorder()
                else -> startRecording()
            }

            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        btnList.setOnClickListener {
            // TODO
            startActivity(Intent(this, GalleryActivity::class.java))

        }

        btnDone.setOnClickListener {
            stopRecorder()
            Toast.makeText(this, "Record saved", Toast.LENGTH_SHORT).show()

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBG.visibility = View.VISIBLE
            filenameInput.setText(filename)
        }

        btnCancel.setOnClickListener {
            File("$dirPath$filename.mp3").delete()
            dismiss()
        }

        btnOk.setOnClickListener {
            dismiss()
            save()
        }

        bottomSheetBG.setOnClickListener {
            File("$dirPath$filename.mp3").delete()
            dismiss()
        }

        btnDelete.setOnClickListener {
            stopRecorder()
            File("$dirPath$filename.mp3").delete()
            Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show()

        }

        btnDelete.isClickable = false
    }

    private fun save(){
        val newFilename = filenameInput.text.toString()
        if(newFilename != filename){
            var newFile = File("$dirPath$newFilename.mp3")
            File("$dirPath$filename.mp3").renameTo(newFile)
        }

        var filePath = "$dirPath$newFilename.mp3"
        var timestamp = Date().time
        var ampsPath = "$dirPath$newFilename"

        try{
            var fos = FileOutputStream(ampsPath)
            var out = ObjectOutputStream(fos)
            out.writeObject(amplitudes)
            fos.close()
            out.close()
        }catch (e :IOException){}

        var record = AudioRecord(newFilename, filePath, timestamp, duration, ampsPath)

        GlobalScope.launch {
            db.audioRecordDao().insert(record)
        }

    }

    private fun dismiss(){
        bottomSheetBG.visibility = View.GONE
        hideKeyboard(filenameInput)

        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }, 100)
    }
    private fun hideKeyboard(view: View){
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE)
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun pauseRecorder(){
        recorder.pause()
        isPaused = true
        btnRecord.setImageResource(R.drawable.ic_record)

        timer.pause()
    }

    private fun resumeRecorder(){
        recorder.resume()
        isPaused = false
        btnRecord.setImageResource(R.drawable.ic_pause)

        timer.start()
    }

    private fun startRecording(){
        if(!permissionGranted){
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }
        // start recording

        recorder = MediaRecorder()
        dirPath = "${externalCacheDir?.absolutePath}/"

        var simpleDateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        var date = simpleDateFormat.format(Date())
        filename = "audio_record_$date"

        recorder.apply{
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$filename.mp3")

            try {
                prepare()
            }catch (e: IOException){}

            start()
        }

        btnRecord.setImageResource(R.drawable.ic_pause)
        isRecording = true
        isPaused = false

        timer.start()

        btnDelete.isClickable = true
        btnDelete.setImageResource(R.drawable.ic_delete)

        btnList.visibility = View.GONE
        btnDone.visibility = View.VISIBLE
    }

    private fun stopRecorder(){
        timer.stop()

        recorder.apply {
            stop()
            release()
        }

        isPaused = false
        isRecording = false

        btnList.visibility = View.VISIBLE
        btnDone.visibility = View.GONE

        btnDelete.isClickable = false
        btnDelete.setImageResource(R.drawable.ic_delete_disabled)

        btnRecord.setImageResource(R.drawable.ic_record)

        tvTimer.text = "00:00.00"
        amplitudes = waveformView.clear()

    }

    override fun onTimerTick(duration: String) {
        tvTimer.text = duration
        this.duration = duration.dropLast(3)
        waveformView.addAmplitude(recorder.maxAmplitude.toFloat())
    }
}