package com.grozziie.videocalling;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

public class MainActivity2 extends AppCompatActivity {
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS =
            {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
            };

    private boolean checkSelfPermission()
    {
        if (ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) !=  PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[1]) !=  PackageManager.PERMISSION_GRANTED)
        {
            return false;
        }
        return true;
    }
    void showMessage(String message) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }
    // Fill the App ID of your project generated on Agora Console.
    private final String appId = "93fdcff4216a4454a1fb2e92a5bfc6b9";
    // Fill the channel name.
    private String channelName = "jj";
    // Fill the temp token generated on Agora Console.
    private String token = "007eJxTYFj2wuq4LEfXl7em5oULjcLitp1Oy/IsmRTNyLLyZ0PehdsKDJbGaSnJaWkmRoZmiSYmpiaJhmlJRqmWRommSWnJZkmWGq5aKQ2BjAwnVxxhYWSAQBCfiSEri4EBAB7iHwM=";
    // An integer that identifies the local user.
    private int uid = 0;
    private boolean isJoined = false;

    private RtcEngine agoraEngine;
    //SurfaceView to render local video in a Container.
    private SurfaceView localSurfaceView;
    //SurfaceView to render Remote video in a Container.
    private SurfaceView remoteSurfaceView;
    // A toggle switch to change the User role.
    private Switch audienceRole;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        audienceRole = (Switch) findViewById(R.id.switch1);
        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }
        setupVideoSDKEngine();
        Button JoinButton=findViewById(R.id.JoinButton);
        JoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission()) {
                    ChannelMediaOptions options = new ChannelMediaOptions();
                    // For Live Streaming, set the channel profile as LIVE_BROADCASTING.
                    options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
                    // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
                    if (audienceRole.isChecked()) { //Audience
                        Toast.makeText(MainActivity2.this, "true", Toast.LENGTH_SHORT).show();
                        options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE;
                        //
                       //remote video







                        FrameLayout container = findViewById(R.id.local_video_view_container);
                        // Create a SurfaceView object and add it as a child to the FrameLayout.
                        localSurfaceView = new SurfaceView(getBaseContext());
                        container.addView(localSurfaceView);
                        // Call setupLocalVideo with a VideoCanvas having uid set to 0.
                        agoraEngine.setupRemoteVideo(new VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
                    } else { //Host
                        Toast.makeText(MainActivity2.this, "false", Toast.LENGTH_SHORT).show();
                        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                        // Display LocalSurfaceView.
                        FrameLayout container = findViewById(R.id.local_video_view_container);
                        // Create a SurfaceView object and add it as a child to the FrameLayout.
                        localSurfaceView = new SurfaceView(getBaseContext());
                        container.addView(localSurfaceView);
                        // Call setupLocalVideo with a VideoCanvas having uid set to 0.
                        agoraEngine.setupLocalVideo(new VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
                        localSurfaceView.setVisibility(View.VISIBLE);
                        // Start local preview.
                        agoraEngine.startPreview();
                    }
                    audienceRole.setEnabled(false); // Disable the switch
                    // Join the channel with a temp token.
                    // You need to specify the user ID yourself, and ensure that it is unique in the channel.
                    agoraEngine.joinChannel(token, channelName, uid, options);
                } else {
                    Toast.makeText(getApplicationContext(), "Permissions was not granted", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    protected void onDestroy() {
        super.onDestroy();
        agoraEngine.stopPreview();
        agoraEngine.leaveChannel();

        // Destroy the engine in a sub-thread to avoid congestion
        new Thread(() -> {
            RtcEngine.destroy();
            agoraEngine = null;
        }).start();
    }
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // Listen for the remote host joining the channel to get the uid of the host.
        public void onUserJoined(int uid, int elapsed) {
            showMessage("Remote user joined " + uid);
            if (!audienceRole.isChecked()) return;
            // Set the remote video view
            runOnUiThread(() -> setupRemoteVideo(uid));
        }
        private void setupRemoteVideo(int uid) {
            FrameLayout container = findViewById(R.id.remote_video_view_container);
            remoteSurfaceView = new SurfaceView(getBaseContext());
            remoteSurfaceView.setZOrderMediaOverlay(true);
            container.addView(remoteSurfaceView);
            agoraEngine.setupRemoteVideo(new VideoCanvas(remoteSurfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
            // Display RemoteSurfaceView.
            remoteSurfaceView.setVisibility(View.VISIBLE);
        }
        public void joinChannel(View view) {
            if (checkSelfPermission()) {
                ChannelMediaOptions options = new ChannelMediaOptions();
                // For Live Streaming, set the channel profile as LIVE_BROADCASTING.
                options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
                // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
                if (audienceRole.isChecked()) { //Audience
                    options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE;
                } else { //Host
                    options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                    // Display LocalSurfaceView.
                    setupLocalVideo();
                    localSurfaceView.setVisibility(View.VISIBLE);
                    // Start local preview.
                    agoraEngine.startPreview();
                }
                audienceRole.setEnabled(false); // Disable the switch
                // Join the channel with a temp token.
                // You need to specify the user ID yourself, and ensure that it is unique in the channel.
                agoraEngine.joinChannel(token, channelName, uid, options);
            } else {
                Toast.makeText(getApplicationContext(), "Permissions was not granted", Toast.LENGTH_SHORT).show();
            }
        }
        public void leaveChannel(View view) {
            if (!isJoined) {
                showMessage("Join a channel first");
            } else {
                agoraEngine.leaveChannel();
                showMessage("You left the channel");
                // Stop remote video rendering.
                if (remoteSurfaceView != null) remoteSurfaceView.setVisibility(View.GONE);
                // Stop local video rendering.
                if (localSurfaceView != null) localSurfaceView.setVisibility(View.GONE);
                isJoined = false;
            }
            audienceRole.setEnabled(true); // Enable the switch
        }
        private void setupLocalVideo() {
            FrameLayout container = findViewById(R.id.local_video_view_container);
            // Create a SurfaceView object and add it as a child to the FrameLayout.
            localSurfaceView = new SurfaceView(getBaseContext());
            container.addView(localSurfaceView);
            // Call setupLocalVideo with a VideoCanvas having uid set to 0.
            agoraEngine.setupLocalVideo(new VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        }
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            isJoined = true;
            showMessage("Joined Channel " + channel);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            showMessage("Remote user offline " + uid + " " + reason);
            runOnUiThread(() -> remoteSurfaceView.setVisibility(View.GONE));
        }
    };
    private void setupVideoSDKEngine() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine.enableVideo();
        } catch (Exception e) {
            showMessage(e.toString());
        }
    }
}