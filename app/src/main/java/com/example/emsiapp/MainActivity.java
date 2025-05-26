package com.example.emsiapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private ImageView professorCharacter;
    private View chatBubbleContainer;
    private TextView chatText;
    private TextView welcomeTitle;
    private TextView appTitle;
    private TextView subtitle;
    private MaterialButton btnLogin;
    private MaterialButton btnRegister;
    private View professorContainer;
    private ImageView floatingElement1, floatingElement2, floatingElement3;
    private View dot1, dot2, dot3;
    private View characterShadow;

    private Handler animationHandler = new Handler();
    private String[] chatMessages = {
            "Bonjour!",
            "Prêt à commencer?",
            "Connectez-vous!",
            "Bienvenue Prof!"
    };
    private int currentMessageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        startAnimations();
        setupClickListeners();
    }

    private void initializeViews() {
        professorCharacter = findViewById(R.id.professor_character);
        chatBubbleContainer = findViewById(R.id.chat_bubble_container);
        chatText = findViewById(R.id.chat_text);
        welcomeTitle = findViewById(R.id.welcome_title);
        appTitle = findViewById(R.id.app_title);
        subtitle = findViewById(R.id.subtitle);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        professorContainer = findViewById(R.id.professor_container);
        floatingElement1 = findViewById(R.id.floating_element_1);
        floatingElement2 = findViewById(R.id.floating_element_2);
        floatingElement3 = findViewById(R.id.floating_element_3);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        characterShadow = findViewById(R.id.character_shadow);
    }

    private void startAnimations() {
        // Initial setup - hide elements
        professorContainer.setAlpha(0f);
        professorContainer.setScaleX(0.5f);
        professorContainer.setScaleY(0.5f);
        welcomeTitle.setAlpha(0f);
        appTitle.setAlpha(0f);
        subtitle.setAlpha(0f);
        btnLogin.setAlpha(0f);
        btnRegister.setAlpha(0f);
        chatBubbleContainer.setAlpha(0f);

        // Start entrance animations
        animateProfessorEntrance();
        animateTextEntrance();
        animateButtonsEntrance();
        startFloatingElementsAnimation();
        startChatBubbleAnimation();
        startDotsAnimation();
        startProfessorIdleAnimation();
    }

    private void animateProfessorEntrance() {
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(professorContainer, "scaleX", 0.5f, 1.2f, 1.0f);
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(professorContainer, "scaleY", 0.5f, 1.2f, 1.0f);
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(professorContainer, "alpha", 0f, 1f);
        ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(professorContainer, "rotation", -10f, 10f, 0f);

        AnimatorSet professorAnimSet = new AnimatorSet();
        professorAnimSet.playTogether(scaleXAnim, scaleYAnim, alphaAnim, rotationAnim);
        professorAnimSet.setDuration(1200);
        professorAnimSet.setInterpolator(new BounceInterpolator());
        professorAnimSet.start();
    }

    private void animateTextEntrance() {
        // Welcome title animation
        ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(welcomeTitle, "alpha", 0f, 1f);
        ObjectAnimator titleTranslation = ObjectAnimator.ofFloat(welcomeTitle, "translationY", 50f, 0f);
        AnimatorSet titleSet = new AnimatorSet();
        titleSet.playTogether(titleAlpha, titleTranslation);
        titleSet.setStartDelay(600);
        titleSet.setDuration(800);
        titleSet.setInterpolator(new AccelerateDecelerateInterpolator());
        titleSet.start();

        // App title animation
        ObjectAnimator appTitleAlpha = ObjectAnimator.ofFloat(appTitle, "alpha", 0f, 1f);
        ObjectAnimator appTitleTranslation = ObjectAnimator.ofFloat(appTitle, "translationY", 50f, 0f);
        ObjectAnimator appTitleScale = ObjectAnimator.ofFloat(appTitle, "scaleX", 0.8f, 1f);
        ObjectAnimator appTitleScaleY = ObjectAnimator.ofFloat(appTitle, "scaleY", 0.8f, 1f);
        AnimatorSet appTitleSet = new AnimatorSet();
        appTitleSet.playTogether(appTitleAlpha, appTitleTranslation, appTitleScale, appTitleScaleY);
        appTitleSet.setStartDelay(800);
        appTitleSet.setDuration(800);
        appTitleSet.setInterpolator(new AccelerateDecelerateInterpolator());
        appTitleSet.start();

        // Subtitle animation
        ObjectAnimator subtitleAlpha = ObjectAnimator.ofFloat(subtitle, "alpha", 0f, 1f);
        ObjectAnimator subtitleTranslation = ObjectAnimator.ofFloat(subtitle, "translationY", 30f, 0f);
        AnimatorSet subtitleSet = new AnimatorSet();
        subtitleSet.playTogether(subtitleAlpha, subtitleTranslation);
        subtitleSet.setStartDelay(1000);
        subtitleSet.setDuration(600);
        subtitleSet.setInterpolator(new AccelerateDecelerateInterpolator());
        subtitleSet.start();
    }

    private void animateButtonsEntrance() {
        // Login button animation
        ObjectAnimator loginAlpha = ObjectAnimator.ofFloat(btnLogin, "alpha", 0f, 1f);
        ObjectAnimator loginTranslation = ObjectAnimator.ofFloat(btnLogin, "translationY", 100f, 0f);
        ObjectAnimator loginScale = ObjectAnimator.ofFloat(btnLogin, "scaleX", 0.8f, 1f);
        AnimatorSet loginSet = new AnimatorSet();
        loginSet.playTogether(loginAlpha, loginTranslation, loginScale);
        loginSet.setStartDelay(1200);
        loginSet.setDuration(600);
        loginSet.setInterpolator(new BounceInterpolator());
        loginSet.start();

        // Register button animation
        ObjectAnimator registerAlpha = ObjectAnimator.ofFloat(btnRegister, "alpha", 0f, 1f);
        ObjectAnimator registerTranslation = ObjectAnimator.ofFloat(btnRegister, "translationY", 100f, 0f);
        ObjectAnimator registerScale = ObjectAnimator.ofFloat(btnRegister, "scaleX", 0.8f, 1f);
        AnimatorSet registerSet = new AnimatorSet();
        registerSet.playTogether(registerAlpha, registerTranslation, registerScale);
        registerSet.setStartDelay(1400);
        registerSet.setDuration(600);
        registerSet.setInterpolator(new BounceInterpolator());
        registerSet.start();
    }

    private void startFloatingElementsAnimation() {
        // Floating animation for decorative elements
        animateFloatingElement(floatingElement1, 2000, 20f);
        animateFloatingElement(floatingElement2, 2500, 15f);
        animateFloatingElement(floatingElement3, 3000, 25f);
    }

    private void animateFloatingElement(View element, int duration, float amplitude) {
        ObjectAnimator floatAnim = ObjectAnimator.ofFloat(element, "translationY", -amplitude, amplitude);
        floatAnim.setDuration(duration);
        floatAnim.setRepeatCount(ObjectAnimator.INFINITE);
        floatAnim.setRepeatMode(ObjectAnimator.REVERSE);
        floatAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        floatAnim.start();

        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(element, "rotation", 0f, 360f);
        rotateAnim.setDuration(duration * 2);
        rotateAnim.setRepeatCount(ObjectAnimator.INFINITE);
        rotateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        rotateAnim.start();
    }

    private void startChatBubbleAnimation() {
        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Show chat bubble
                ObjectAnimator bubbleAlpha = ObjectAnimator.ofFloat(chatBubbleContainer, "alpha", 0f, 1f);
                ObjectAnimator bubbleScale = ObjectAnimator.ofFloat(chatBubbleContainer, "scaleX", 0f, 1f);
                ObjectAnimator bubbleScaleY = ObjectAnimator.ofFloat(chatBubbleContainer, "scaleY", 0f, 1f);
                AnimatorSet bubbleSet = new AnimatorSet();
                bubbleSet.playTogether(bubbleAlpha, bubbleScale, bubbleScaleY);
                bubbleSet.setDuration(300);
                bubbleSet.setInterpolator(new BounceInterpolator());
                bubbleSet.start();

                // Start message cycling
                startMessageCycling();
            }
        }, 1500);
    }

    private void startMessageCycling() {
        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Fade out current message
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(chatText, "alpha", 1f, 0f);
                fadeOut.setDuration(200);
                fadeOut.start();

                // Change message and fade in
                animationHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        currentMessageIndex = (currentMessageIndex + 1) % chatMessages.length;
                        chatText.setText(chatMessages[currentMessageIndex]);

                        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(chatText, "alpha", 0f, 1f);
                        fadeIn.setDuration(200);
                        fadeIn.start();
                    }
                }, 200);

                // Schedule next message change
                animationHandler.postDelayed(this, 2500);
            }
        }, 2500);
    }

    private void startDotsAnimation() {
        animateDotsSequence();
    }

    private void animateDotsSequence() {
        ObjectAnimator dot1Anim = ObjectAnimator.ofFloat(dot1, "scaleX", 1f, 1.5f, 1f);
        ObjectAnimator dot1AnimY = ObjectAnimator.ofFloat(dot1, "scaleY", 1f, 1.5f, 1f);
        AnimatorSet dot1Set = new AnimatorSet();
        dot1Set.playTogether(dot1Anim, dot1AnimY);
        dot1Set.setDuration(600);
        dot1Set.setStartDelay(0);

        ObjectAnimator dot2Anim = ObjectAnimator.ofFloat(dot2, "scaleX", 1f, 1.5f, 1f);
        ObjectAnimator dot2AnimY = ObjectAnimator.ofFloat(dot2, "scaleY", 1f, 1.5f, 1f);
        AnimatorSet dot2Set = new AnimatorSet();
        dot2Set.playTogether(dot2Anim, dot2AnimY);
        dot2Set.setDuration(600);
        dot2Set.setStartDelay(200);

        ObjectAnimator dot3Anim = ObjectAnimator.ofFloat(dot3, "scaleX", 1f, 1.5f, 1f);
        ObjectAnimator dot3AnimY = ObjectAnimator.ofFloat(dot3, "scaleY", 1f, 1.5f, 1f);
        AnimatorSet dot3Set = new AnimatorSet();
        dot3Set.playTogether(dot3Anim, dot3AnimY);
        dot3Set.setDuration(600);
        dot3Set.setStartDelay(400);

        AnimatorSet dotsSequence = new AnimatorSet();
        dotsSequence.playTogether(dot1Set, dot2Set, dot3Set);
        dotsSequence.start();

        // Repeat animation
        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animateDotsSequence();
            }
        }, 3000);
    }

    private void startProfessorIdleAnimation() {
        // Subtle breathing/idle animation for the professor
        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator breatheScale = ObjectAnimator.ofFloat(professorCharacter, "scaleX", 1f, 1.02f, 1f);
                ObjectAnimator breatheScaleY = ObjectAnimator.ofFloat(professorCharacter, "scaleY", 1f, 1.02f, 1f);
                AnimatorSet breatheSet = new AnimatorSet();
                breatheSet.playTogether(breatheScale, breatheScaleY);
                breatheSet.setDuration(2000);
                breatheSet.setInterpolator(new AccelerateDecelerateInterpolator());
                breatheSet.start();

                // Repeat breathing animation
                animationHandler.postDelayed(this, 3000);
            }
        }, 2000);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButtonClick(v);
                animationHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, Login.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                }, 200);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButtonClick(v);
                animationHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                }, 200);
            }
        });

        // Add click animation to professor character
        professorCharacter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fun interaction - professor wave animation
                ObjectAnimator wave = ObjectAnimator.ofFloat(v, "rotation", 0f, -15f, 15f, -10f, 10f, 0f);
                wave.setDuration(800);
                wave.setInterpolator(new BounceInterpolator());
                wave.start();

                // Change chat message to greeting
                chatText.setText("👋 Salut!");
                ObjectAnimator pulse = ObjectAnimator.ofFloat(chatBubbleContainer, "scaleX", 1f, 1.2f, 1f);
                ObjectAnimator pulseY = ObjectAnimator.ofFloat(chatBubbleContainer, "scaleY", 1f, 1.2f, 1f);
                AnimatorSet pulseSet = new AnimatorSet();
                pulseSet.playTogether(pulse, pulseY);
                pulseSet.setDuration(300);
                pulseSet.start();
            }
        });
    }

    private void animateButtonClick(View button) {
        ObjectAnimator scaleDown = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f);
        ObjectAnimator scaleUp = ObjectAnimator.ofFloat(button, "scaleX", 0.95f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.95f, 1f);

        AnimatorSet scaleDownSet = new AnimatorSet();
        scaleDownSet.playTogether(scaleDown, scaleDownY);
        scaleDownSet.setDuration(100);

        AnimatorSet scaleUpSet = new AnimatorSet();
        scaleUpSet.playTogether(scaleUp, scaleUpY);
        scaleUpSet.setDuration(100);

        AnimatorSet clickAnimSet = new AnimatorSet();
        clickAnimSet.playSequentially(scaleDownSet, scaleUpSet);
        clickAnimSet.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }
    }
}