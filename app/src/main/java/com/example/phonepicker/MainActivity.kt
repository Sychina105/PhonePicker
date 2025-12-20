package com.example.phonepicker  // ← Убедись, что пакет совпадает с твоим!

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val pickers = mutableListOf<NumberPicker>()
    private lateinit var phoneText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        phoneText = findViewById(R.id.phoneNumberText)

        // Собираем все 10 колёсиков
        pickers.addAll(listOf(
            findViewById(R.id.digit1), findViewById(R.id.digit2), findViewById(R.id.digit3),
            findViewById(R.id.digit4), findViewById(R.id.digit5), findViewById(R.id.digit6),
            findViewById(R.id.digit7), findViewById(R.id.digit8),
            findViewById(R.id.digit9), findViewById(R.id.digit10)
        ))

        // Настройка каждого колёсика
        pickers.forEach { picker ->
            picker.minValue = 0
            picker.maxValue = 9
            picker.wrapSelectorWheel = true
            picker.setOnValueChangedListener { _, _, _ -> updatePhoneNumber() }
        }

        updatePhoneNumber() // Начальный номер

        // Кнопка "Случайный номер!" — запускает анимацию спина
        findViewById<Button>(R.id.randomButton).setOnClickListener {
            generateSlotMachineAnimation()
        }

        // Кнопка "Позвонить!"
        findViewById<Button>(R.id.submitButton).setOnClickListener {
            val fullNumber = "+7${getPhoneNumber()}"
            Toast.makeText(this, "Звонок на: $fullNumber", Toast.LENGTH_LONG).show()
        }
    }

    private fun updatePhoneNumber() {
        val number = getPhoneNumber()
        phoneText.text = "+7 (${number.substring(0,3)}) ${number.substring(3,6)}-${number.substring(6,8)}-${number.substring(8,10)}"
    }

    private fun getPhoneNumber(): String {
        return pickers.joinToString("") { it.value.toString() }
    }

    // Анимация слот-машины: колёсики крутятся и останавливаются на случайных цифрах
    private fun generateSlotMachineAnimation() {
        val random = Random.Default

        pickers.forEachIndexed { index, picker ->
            val delay = index * 100L  // Задержка для волны (каждое следующее позже)

            picker.postDelayed({
                val targetValue = random.nextInt(0, 10)  // Случайная цифра 0-9
                val spins = random.nextInt(3, 6)  // 3-5 полных оборотов
                val animatedValue = picker.value + spins * 10 + targetValue

                // Анимация прокрутки
                val animator = ValueAnimator.ofInt(picker.value, animatedValue)
                animator.duration = 1500L + random.nextLong(500L)  // 1.5-2 секунды
                animator.interpolator = DecelerateInterpolator()  // Замедление в конце
                animator.addUpdateListener { animation ->
                    picker.value = ((animation.animatedValue as Int) % 10).coerceIn(0, 9)
                }
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        picker.value = targetValue
                        updatePhoneNumber()
                    }
                })
                animator.start()
            }, delay)
        }
    }
}