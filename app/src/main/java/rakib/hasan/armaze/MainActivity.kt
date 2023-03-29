package rakib.hasan.armaze

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.util.*


class MainActivity : AppCompatActivity() {

    // object of ArFragment Class
    private var arCam: ArFragment? = null

    // helps to render the 3d model
    // only once when we tap the screen
    private var clickNo = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkSystemSupport(this)) {

            // ArFragment is linked up with its respective id used in the activity_main.xml
            arCam = supportFragmentManager.findFragmentById(R.id.arCameraArea) as ArFragment?
            arCam!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
                clickNo++
                // the 3d model comes to the scene only
                // when clickNo is one that means once
                if (clickNo == 1) {
                    val anchor = hitResult.createAnchor()
                    ModelRenderable.builder()
                        .setSource(this, R.raw.gfg_gold_text_stand_2)
                        .setIsFilamentGltf(true)
                        .build()
                        .thenAccept { modelRenderable: ModelRenderable? ->
                            addModel(
                                anchor,
                                modelRenderable!!
                            )
                        }
                        .exceptionally { throwable: Throwable ->
                            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                            builder.setMessage("Something is not right" + throwable.message).show()
                            null
                        }
                }
            }
        } else {
            return
        }

    }

    private fun checkSystemSupport(activity: Activity): Boolean {

        // checking whether the API version of the running Android >= 24
        // that means Android Nougat 7.0
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val openGlVersion: String = (Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE)) as ActivityManager).deviceConfigurationInfo.glEsVersion

            // checking whether the OpenGL version >= 3.0
            if (openGlVersion.toDouble() >= 3.0) {
                true
            } else {
                Toast.makeText(
                    activity,
                    "App needs OpenGl Version 3.0 or later",
                    Toast.LENGTH_SHORT
                ).show()
                activity.finish()
                false
            }
        } else {
            Toast.makeText(
                activity,
                "App does not support required Build Version",
                Toast.LENGTH_SHORT
            ).show()
            activity.finish()
            false
        }
    }

    private fun addModel(anchor: Anchor, modelRenderable: ModelRenderable) {

        // Creating a AnchorNode with a specific anchor
        val anchorNode = AnchorNode(anchor)

        // attaching the anchorNode with the ArFragment
        anchorNode.setParent(arCam!!.arSceneView.scene)

        // attaching the anchorNode with the TransformableNode
        val model = TransformableNode(arCam!!.transformationSystem)
        model.setParent(anchorNode)

        // attaching the 3d model with the TransformableNode
        // that is already attached with the node
        model.renderable = modelRenderable
        model.select()
    }
}