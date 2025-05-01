package com.github.bobryanskiy.tamagotchiforlovers

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import androidx.core.view.doOnLayout
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import com.github.bobryanskiy.tamagotchiforlovers.notifications.Notifications
import java.util.Calendar

class DifficultyChooseFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_difficulty_choose, container, false)
    }

    var starAnimateX: FlingAnimation? = null
    var starAnimateY: FlingAnimation? = null
    var velocityTracker: VelocityTracker? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.notb).setOnClickListener {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.SECOND, 0)
            }
            val intent = Intent(context, GameModeChooseFragment::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            context?.let { it1 -> Notifications.PetWantEat.schedule(it1, calendar.timeInMillis, "Привет", "Я проголодался", pendingIntent) }
        }

        view.findViewById<View>(R.id.star).let { star ->

            val rect = Rect()
            val rect2 = Rect()


            star.getHitRect(rect)
            view.getHitRect(rect2)

            star.doOnLayout {
                starAnimateX = FlingAnimation(star, DynamicAnimation.X).apply {
                    friction = 0.5f
                    setMinValue(0f)
                    setMaxValue(view.width.toFloat() - star.width.toFloat())
                }
                starAnimateY = FlingAnimation(star, DynamicAnimation.Y).apply {
                    friction = 0.5f
                    setMinValue(0f)
                    setMaxValue(view.height.toFloat() - star.width.toFloat())
                }
            }
            var oldX = star.x
            var oldY = star.y
            star.setOnTouchListener { v, event ->
                v.performClick()
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        starAnimateX?.cancel()
                        starAnimateY?.cancel()
                        velocityTracker = VelocityTracker.obtain()
                        velocityTracker!!.addMovement(event)
                        oldX = event.x
                        oldY = event.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        velocityTracker!!.addMovement(event)
                        println(" $rect")
                        println(rect2)
                        if (rect2.contains(rect)) {
                            v.x += event.x - oldX
                            v.y += event.y - oldY
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        velocityTracker!!.addMovement(event)
                        velocityTracker!!.computeCurrentVelocity(1000)
                        starAnimateX!!.apply {
                            setStartVelocity(velocityTracker!!.xVelocity)
                            start()
                        }
                        starAnimateY!!.apply {
                            setStartVelocity(velocityTracker!!.yVelocity)
                            start()
                        }
                        velocityTracker!!.recycle()
                    }
                }
                true
            }
        }

        view.findViewById<Button>(R.id.button5).setOnClickListener { button ->
//            setStartVelocity(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.001f, resources.displayMetrics))
            SpringAnimation(button, DynamicAnimation.TRANSLATION_Y, 1000f).apply {
                spring = SpringForce(1000f).apply {
                    dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                    stiffness = SpringForce.STIFFNESS_MEDIUM
                }
//                start()
            }
            AnimatorSet().apply {
                play(
                    ObjectAnimator.ofFloat(
                        button,
                        View.X,
                        100f,
                        400f
                    )
                ).apply {
                    with(ObjectAnimator.ofFloat(button, View.Y, 100F, 400F))
                    with(ObjectAnimator.ofFloat(button, View.SCALE_X, 0f, 2f))
                    with(ObjectAnimator.ofFloat(button, View.SCALE_Y, 0f, 2f))
                }
                duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
                interpolator = DecelerateInterpolator()
                start()
            }

            val animationX = ObjectAnimator.ofFloat(button, View.X, 0F)
            val animationY = ObjectAnimator.ofFloat(button, View.Y, 0F)
            val set = AnimatorSet()
            set.play(animationX).with(animationY).with(ObjectAnimator.ofFloat(button, View.SCALE_X, 2f, 1f)).with(ObjectAnimator.ofFloat(button, View.SCALE_Y, 2f, 1f))
            set.duration = 500
            set.interpolator = DecelerateInterpolator()
            set.startDelay = 1000
            set.start()
        }
    }
}