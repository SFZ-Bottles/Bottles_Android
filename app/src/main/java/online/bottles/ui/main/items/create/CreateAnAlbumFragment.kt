package online.bottles.ui.main.items.create

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import online.bottles.R
import online.bottles.databinding.FragmentCreateAnAlbumBinding
import online.bottles.ui.base.BaseFragment
import online.bottles.ui.main.MainActivity
import org.json.JSONArray
import org.json.JSONObject

class CreateAnAlbumFragment : BaseFragment() {
    private lateinit var binding: FragmentCreateAnAlbumBinding
    private val REQ_GALLERY = 1001
    private val REQ_STORAGE_PERMISSION = 1002
    private var selectedImageType = 0 //0->선언안됨, 1->titleImage,2->image

    private var imageUri : Uri? = null
    private var titleImageUri : Uri? = null
    //private var jsonObject
    private var albumObject = JSONObject()
    private var albumArray = JSONArray()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =FragmentCreateAnAlbumBinding.inflate(inflater, container, false)
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
    private fun selectGallery() {
        var writePermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        var readPermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)

        if (writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED) {
            // 권한이 없으면 요청
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), REQ_STORAGE_PERMISSION)
        } else {
            // 권한이 있으면 갤러리 열기
            val intent = Intent(Intent.ACTION_PICK)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.type = "image/*"
            startActivityForResult(intent, REQ_GALLERY)
        }
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
                }

            }
        }
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
        albumArray.removeAll()//엘범어래이 모두 삭제
        super.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        albumObject.put("is_private",false) //일반 게시글을 표시

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
            selectGallery()
        }
        //image를 고르는 동작
        binding.imageUpload.setOnClickListener(){
            selectedImageType = 2
            selectGallery()
        }
        //타이틀 작성 완료 후 Add 버튼을 누를 때
        binding.addTitle.setOnClickListener(){
        }
        //Text 작성 완료 후 Add 버튼을 누를 때
        binding.addText.setOnClickListener(){

        }
        //image 작성 완료 후 Add 버튼을 누를 때
        binding.addImage.setOnClickListener(){
            albumObject.put()
        }
        //Video 작성 완료 후 Add 버튼을 누를 때
        binding.addVideo.setOnClickListener(){}


    }
    fun iconVisible(value:Int) {
        val selectTitle = binding.selectedTitle//첫 번째 인자 title 테두리
        val selectText = binding.selectedText//text 테두리
        val selectImage = binding.selectedImage
        val selectVideo = binding.selectedVideo

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
        }
    }
}

fun JSONArray.removeAll() {
    for (i in 0 until length()) {
        remove(0)
    }
}

