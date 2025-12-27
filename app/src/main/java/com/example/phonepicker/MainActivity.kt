package com.example.phonepicker  // ← Твой пакет!

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
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
    private lateinit var coinsText: TextView
    private var coins = 0
    private val SPIN_COST = 10
    private val prefsName = "PhoneSlotPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        phoneText = findViewById(R.id.phoneNumberText)
        coinsText = findViewById(R.id.coinsText)

        // Загружаем монетки
        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        coins = prefs.getInt("coins", 20)  // Стартуем с 20 монеток
        updateCoinsDisplay()

        // Колёсики
        pickers.addAll(listOf(
            findViewById(R.id.digit1), findViewById(R.id.digit2), findViewById(R.id.digit3),
            findViewById(R.id.digit4), findViewById(R.id.digit5), findViewById(R.id.digit6),
            findViewById(R.id.digit7), findViewById(R.id.digit8),
            findViewById(R.id.digit9), findViewById(R.id.digit10)
        ))

        pickers.forEach { picker ->
            picker.minValue = 0
            picker.maxValue = 9
            picker.wrapSelectorWheel = true
            picker.setOnValueChangedListener { _, _, _ -> updatePhoneNumber() }
        }

        updatePhoneNumber()

        // Кнопка-кликер: +1 монетка
        findViewById<Button>(R.id.clickerButton).setOnClickListener {
            coins++
            updateCoinsDisplay()
            saveCoins()

            // Забавная анимация +1 (можно расширить)
            Toast.makeText(this, "+1 💰", Toast.LENGTH_SHORT).show()
        }

        // Кнопка СПИН!
        findViewById<Button>(R.id.spinButton).setOnClickListener {
            if (coins >= SPIN_COST) {
                coins -= SPIN_COST
                updateCoinsDisplay()
                saveCoins()
                generateSlotMachineAnimation()
            } else {
                Toast.makeText(this, "Недостаточно монеток! Кликай больше 👆", Toast.LENGTH_LONG).show()
            }
        }

        // Позвонить
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

    private fun updateCoinsDisplay() {
        coinsText.text = "Монетки: $coins 💰"
    }

    private fun saveCoins() {
        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        prefs.edit().putInt("coins", coins).apply()
    }

    // Анимация слот-машины
    private fun generateSlotMachineAnimation() {
        val random = Random.Default

        pickers.forEachIndexed { index, picker ->
            val delay = index * 100L

            picker.postDelayed({
                val targetValue = random.nextInt(0, 10)
                val spins = random.nextInt(3, 6)
                val animatedValue = picker.value + spins * 10 + targetValue

                val animator = ValueAnimator.ofInt(picker.value, animatedValue)
                animator.duration = 1500L + random.nextLong(600L)
                animator.interpolator = DecelerateInterpolator()
                animator.addUpdateListener {
                    picker.value = ((it.animatedValue as Int) % 10).coerceIn(0, 9)
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