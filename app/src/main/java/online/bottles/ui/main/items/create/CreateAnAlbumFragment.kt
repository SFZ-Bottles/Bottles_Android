package online.bottles.ui.main.items.create

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.VideoView;
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import online.bottles.R
import online.bottles.databinding.FragmentCreateAnAlbumBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.main.MainActivity
import org.json.JSONObject


class CreateAnAlbumFragment : BaseFragment() {
    private lateinit var binding: FragmentCreateAnAlbumBinding
    private val REQ_GALLERY = 1001
    private var uri = "https://192.168.0.1"
    private val REQ_STORAGE_PERMISSION = 1002
    private var selectedImageType = 0 //0->선언안됨, 1->titleImage,2->image
    private var albumOrder = 0// createAlbum Object들의 순서를 명시
    private lateinit var callback: OnBackPressedCallback//뒤로가기 커스텀
    private var imageUri : Uri? = null
    private var videoUri :Uri? = null
    private var titleImageUri : Uri? = null
    //private var jsonObject
    private var albumData = JSONObject()//album의 실질적인 data 보내질 때 data,albumData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =FragmentCreateAnAlbumBinding.inflate(inflater, container, false)


        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뒤로가기 클릭시 동작하는 로직
                if(albumOrder > 0){
                    showExitDialog()
                } else {
                    justBack()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)
        return binding.root
    }

    private fun checkPermissionAndRequest() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없는 경우 권한 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                101 // 요청 코드 (임의로 선택 가능)
            )
        } else {
            // 권한이 이미 승인된 경우, 원하는 작업 수행
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            101 -> {
                // 외부 저장소 읽기 권한 요청 결과 처리
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 승인된 경우, 원하는 작업 수행
                } else {
                    // 권한이 거부된 경우, 사용자에게 알림 표시 또는 다른 조치 수행
                }
            }
        }
    }
    private fun checkAndOpenGallery(mimeType: String) {
        var writePermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        var readPermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)

        if (writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED) {
            // 권한이 없으면 요청
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), REQ_STORAGE_PERMISSION)
        } else {
            // 권한이 있으면 갤러리 열기
            val intent = Intent(Intent.ACTION_PICK)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.type = mimeType
            startActivityForResult(intent, REQ_GALLERY)
        }
    }

    private fun selectImage() {
        checkAndOpenGallery("image/*")
    }

    private fun selectVideo() {
        checkAndOpenGallery("video/*")
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_GALLERY && resultCode == Activity.RESULT_OK) {
            // 이미지 선택 결과 처리
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                // 선택된 이미지를 처리하는 로직.
                when(selectedImageType){
                    1 ->{ binding.titleImageSrc.setImageURI(selectedImageUri)
                        titleImageUri = selectedImageUri}// 이미지 uri 잠시동안 저장
                    2-> { binding.imageSrc.setImageURI(selectedImageUri)
                        imageUri = selectedImageUri}
                    3->{ val video = binding.videoSrc
                        val mc = MediaController(requireContext()) // 비디오 컨트롤 가능하게(일시정지, 재시작 등)
                        video.setMediaController(mc)
                        video.setVideoPath(selectedImageUri.toString())
                        video.start()
                        videoUri = selectedImageUri
                        binding.testtext.setText( videoUri.toString())}
                }

            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

    }
    override fun onStart(){
        super.onStart()
        checkPermissionAndRequest()
    }
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.createAnAlbumFragmentOnResume()
    }
    override fun onStop() {
            (activity as? MainActivity)?.myPageFragmentOnResume()
            super.onStop()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //뒤로가기 버튼
        binding.backButton.setOnClickListener(){
           requireActivity().onBackPressed()
        }
        //타이틀 선택시
        binding.addTitleButton.setOnClickListener(){
            iconVisible(1)
        }
        //text 선택
        binding.addTextButton.setOnClickListener(){
            iconVisible(2)
        }
        //image 선택시
        binding.addImageButton.setOnClickListener(){
            iconVisible(3)
        }
        //video 선택시
        binding.addVideoButton.setOnClickListener(){
            iconVisible(4)
        }
        //타이틀 이미지 고르는 동작
        binding.titleImageUpload.setOnClickListener(){
            selectedImageType = 1
            selectImage()
        }
        //image를 고르는 동작
        binding.imageUpload.setOnClickListener(){
            selectedImageType = 2
            selectImage()
        }
        //video를 고르는 동작
        binding.videoUpload.setOnClickListener(){
            selectedImageType = 3
            selectVideo()
        }
        //타이틀(cover) 작성 완료 후 Add 버튼을 누를 때
        binding.addTitle.setOnClickListener(){
            val titleObject = JSONObject()
            titleObject.put("order",++albumOrder)
            titleObject.put("species","cover")
            titleObject.put("data",titleImageUri)
            albumData.put("pages",titleObject)
            iconVisible(5)

            val includeTemplate = layoutInflater.inflate(R.layout.add_template_cover,null)
            binding.includeTemplate.addView(includeTemplate)
            val setOrder = includeTemplate.findViewById<TextView>(R.id.order)
            setOrder.text ="<"+albumOrder+"/12>"
            binding.text.text.clear()

        }

        //Text 작성 완료 후 Add 버튼을 누를 때
        binding.addText.setOnClickListener(){
            val text = binding.text.text.toString()
            if(albumOrder >= 12){
                showMaxItemCountDialog()
                iconVisible(5)
            }else{ if (text.isEmpty()){
                showTextIsNullDialog()
            }else{
                val textData = JSONObject()
                textData.put("order",++albumOrder)
                textData.put("species","text")
                textData.put("data",text)
                albumData.put("pages",textData)
                iconVisible(5)


                val includeTemplate = layoutInflater.inflate(R.layout.add_template_text,null)
                binding.includeTemplate.addView(includeTemplate)
                val setOrder = includeTemplate.findViewById<TextView>(R.id.order)
                setOrder.text ="<"+albumOrder+"/12>"
                binding.text.text.clear() //적혀있는 글 삭제
                }
            }

        }
        //image 작성 완료 후 Add 버튼을 누를 때
        binding.addImage.setOnClickListener(){
            if(albumOrder >= 12){
                showMaxItemCountDialog()
                iconVisible(5)
            }else{
                val imageData = JSONObject()
                //image 값이 기본 값일 경우 다이얼로그 출력

                imageData.put("order",++albumOrder)
                imageData.put("species","image")
                imageData.put("data",imageUri)
                albumData.put("pages",imageData)
                iconVisible(5)

                //해당 imageView에 표시된 사진 원상복구하기
                val resourceId = R.drawable.test
                val defaultImage = ContextCompat.getDrawable(requireContext(), resourceId)
                binding.imageSrc.setImageDrawable(defaultImage)

                //include 작업
                val includeTemplate = layoutInflater.inflate(R.layout.add_template_image,null)
                binding.includeTemplate.addView(includeTemplate)
                val setOrder = includeTemplate.findViewById<TextView>(R.id.order)
                setOrder.text ="<"+albumOrder+"/12>"}


        }
        //Video 작성 완료 후 Add 버튼을 누를 때
        binding.addVideo.setOnClickListener(){
            if(albumOrder >= 12){
                showMaxItemCountDialog()
                iconVisible(5)
            }else{
                val videoData = JSONObject()
                videoData.put("order",++albumOrder)
                videoData.put("species","video")
                videoData.put("data",videoUri)
                albumData.put("pages",videoData)
                iconVisible(5)
                //video 뷰 원상복구
                binding.videoSrc.setVideoPath("asd")

                val includeTemplate = layoutInflater.inflate(R.layout.add_template_video,null)
                binding.includeTemplate.addView(includeTemplate)
                val setOrder = includeTemplate.findViewById<TextView>(R.id.order)
                setOrder.text ="<"+albumOrder+"/12>"}
        }
    }
    fun iconVisible(value:Int) {
        val selectTitle = binding.selectedTitle//첫 번째 인자 title 테두리
        val selectText = binding.selectedText//text 테두리
        val selectImage = binding.selectedImage
        val selectVideo = binding.selectedVideo
        val selectedOptions = binding.selectedOptions

        val includeTemplate = binding.includeTemplate//두 번째 선택 목록 창

        val createTitle = binding.createTitle//title 만드는 화면 초기상태 gone
        val createText = binding.createText//text 만드는 화면 gone
        val createImage = binding.createImage//image 만드는 화면//
        val createVideo = binding.createVideo//video//

        // 각각 인자의 add 버튼
        val addTitle = binding.addTitle
        val addText = binding.addText
        val addImage = binding.addImage
        val addVideo = binding.addVideo
        val previewButton = binding.previewButton// 미리보기 버튼
        val postButton = binding.postButton//게시글 등록버튼

        when (value) {
            //title 아이콘이 선택되었을 때
            1 -> {
                //Title 과 관련하지 않은 모든 인자 비활성화
                val addButtonPosition = addTitle.layoutParams as RelativeLayout.LayoutParams
                includeTemplate.visibility = View.GONE
                createTitle.visibility = View.VISIBLE
                createText.visibility = View.GONE
                createImage.visibility = View.GONE
                createVideo.visibility = View.GONE

                selectedOptions.visibility = View.GONE
                selectTitle.visibility = View.VISIBLE
                selectText.visibility = View.GONE
                selectImage.visibility = View.GONE
                selectVideo.visibility = View.GONE

                addTitle.visibility = View.VISIBLE
                addText.visibility = View.GONE
                addImage.visibility = View.GONE
                addVideo.visibility = View.GONE
                addButtonPosition.addRule(RelativeLayout.BELOW, R.id.createTitle)
                addTitle.layoutParams = addButtonPosition
                previewButton.visibility = View.GONE
                postButton.visibility = View.GONE
            }
            //Text 아이콘이 선택되었을 때
            2 -> {
                val addButtonPosition = addText.layoutParams as RelativeLayout.LayoutParams
                includeTemplate.visibility = View.GONE
                createTitle.visibility = View.GONE
                createText.visibility = View.VISIBLE
                createImage.visibility = View.GONE
                createVideo.visibility = View.GONE

                selectedOptions.visibility = View.GONE
                selectTitle.visibility = View.GONE
                selectText.visibility = View.VISIBLE
                selectImage.visibility = View.GONE
                selectVideo.visibility = View.GONE

                addTitle.visibility = View.GONE
                addText.visibility = View.VISIBLE
                addImage.visibility = View.GONE
                addVideo.visibility = View.GONE

                addButtonPosition.addRule(RelativeLayout.BELOW, R.id.createText)
                addText.layoutParams = addButtonPosition
                previewButton.visibility = View.GONE
                postButton.visibility = View.GONE
            }
            //image 아이콘 선택
            3 -> {
                val addButtonPosition = addImage.layoutParams as RelativeLayout.LayoutParams
                includeTemplate.visibility = View.GONE
                createTitle.visibility = View.GONE
                createText.visibility = View.GONE
                createImage.visibility = View.VISIBLE
                createVideo.visibility = View.GONE

                selectedOptions.visibility = View.GONE
                selectTitle.visibility = View.GONE
                selectText.visibility = View.GONE
                selectImage.visibility = View.VISIBLE
                selectVideo.visibility = View.GONE

                addTitle.visibility = View.GONE
                addText.visibility = View.GONE
                addImage.visibility = View.VISIBLE
                addVideo.visibility = View.GONE
                addButtonPosition.addRule(RelativeLayout.BELOW, R.id.createImage)
                addImage.layoutParams = addButtonPosition
                previewButton.visibility = View.GONE
                postButton.visibility = View.GONE
            }
            //video 선택
            4 -> {
                val addButtonPosition = addVideo.layoutParams as RelativeLayout.LayoutParams
                includeTemplate.visibility = View.GONE
                createTitle.visibility = View.GONE
                createText.visibility = View.GONE
                createImage.visibility = View.GONE
                createVideo.visibility = View.VISIBLE

                selectedOptions.visibility = View.GONE
                selectTitle.visibility = View.GONE
                selectText.visibility = View.GONE
                selectImage.visibility = View.GONE
                selectVideo.visibility = View.VISIBLE

                addTitle.visibility = View.GONE
                addText.visibility = View.GONE
                addImage.visibility = View.GONE
                addVideo.visibility = View.VISIBLE
                addButtonPosition.addRule(RelativeLayout.BELOW, R.id.createVideo)
                addVideo.layoutParams = addButtonPosition
                previewButton.visibility = View.GONE
                postButton.visibility = View.GONE
            }
            //아무것도 선택되지 않은 상태
            5->{
                includeTemplate.visibility = View.VISIBLE
                createTitle.visibility = View.GONE
                createText.visibility = View.GONE
                createImage.visibility = View.GONE
                createVideo.visibility = View.GONE

                selectedOptions.visibility = View.VISIBLE
                selectTitle.visibility = View.GONE
                selectText.visibility = View.GONE
                selectImage.visibility = View.GONE
                selectVideo.visibility = View.GONE

                addTitle.visibility = View.GONE
                addText.visibility = View.GONE
                addImage.visibility = View.GONE
                addVideo.visibility = View.GONE
                previewButton.visibility = View.VISIBLE
                postButton.visibility = View.VISIBLE
                if(albumOrder >= 1){
                    binding.addTitleButton.visibility = View.GONE
                }
                else{
                    binding.addTitleButton.visibility = View.VISIBLE
                }
            }
        }
    }
    private fun showTextIsNullDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("text를 입력해주세요.")
            .setCancelable(false)
            .setPositiveButton("확인") { dialog, id ->
                // 다이얼로그 닫기
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }
    //엘범 수 12개 이상 추가하려고 하면 막는거
    private fun showMaxItemCountDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("추가할 수 있는 엘범의 수가 초과되었습니다. (최대 12개)")
            .setCancelable(false)
            .setPositiveButton("확인") { dialog, id ->
                // 다이얼로그 닫기
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }
    private fun showExitDialog(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("작성하는 내역을 삭제하고 종료하시겠습니까?")
            .setCancelable(false)
            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                // 사용자가 확인 버튼을 누르면 앱 종료
                callback.isEnabled=false
                callback.remove()
                requireActivity().onBackPressed()
            })
            .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id ->
                // 사용자가 취소 버튼을 누르면 다이얼로그 닫기
                dialog.dismiss()
            })

        val alertDialog = builder.create()
        alertDialog.show()
    }
    private fun justBack(){
        callback.isEnabled=false
        callback.remove()
        requireActivity().onBackPressed()
    }
}


