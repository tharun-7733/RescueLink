import React, { useEffect, useRef } from 'react';
import { View, StyleSheet, Animated, Easing } from 'react-native';
import { colors, animation } from '../theme/tokens';

/**
 * Animated atmospheric background:
 * - Two pulsing radial blobs (top-left red, bottom-right deep-red)
 * - Dot grid overlay
 * - Diagonal slash accent
 */
export default function BackgroundLayer() {
  // Blob 1 pulse
  const blob1Anim = useRef(new Animated.Value(0)).current;
  // Blob 2 pulse (offset)
  const blob2Anim = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    const pulse = (anim: Animated.Value, delay = 0) =>
      Animated.loop(
        Animated.sequence([
          Animated.timing(anim, {
            toValue: 1,
            duration: animation.glow,
            delay,
            easing: Easing.inOut(Easing.ease),
            useNativeDriver: true,
          }),
          Animated.timing(anim, {
            toValue: 0,
            duration: animation.glow,
            easing: Easing.inOut(Easing.ease),
            useNativeDriver: true,
          }),
        ])
      );

    const a1 = pulse(blob1Anim, 0);
    const a2 = pulse(blob2Anim, 2500);
    a1.start();
    a2.start();
    return () => { a1.stop(); a2.stop(); };
  }, [blob1Anim, blob2Anim]);

  const blob1Scale = blob1Anim.interpolate({ inputRange: [0, 1], outputRange: [1, 1.08] });
  const blob1Opacity = blob1Anim.interpolate({ inputRange: [0, 1], outputRange: [0.14, 0.24] });
  const blob2Scale = blob2Anim.interpolate({ inputRange: [0, 1], outputRange: [1, 1.08] });
  const blob2Opacity = blob2Anim.interpolate({ inputRange: [0, 1], outputRange: [0.14, 0.24] });

  return (
    <View style={StyleSheet.absoluteFill} pointerEvents="none">
      {/* Blob 1 – top-left */}
      <Animated.View
        style={[
          styles.blob,
          styles.blob1,
          { opacity: blob1Opacity, transform: [{ scale: blob1Scale }] },
        ]}
      />
      {/* Blob 2 – bottom-right */}
      <Animated.View
        style={[
          styles.blob,
          styles.blob2,
          { opacity: blob2Opacity, transform: [{ scale: blob2Scale }] },
        ]}
      />
      {/* Diagonal slash */}
      <View style={styles.slash} />
    </View>
  );
}

const BLOB_SIZE = 420;

const styles = StyleSheet.create({
  blob: {
    position: 'absolute',
    width: BLOB_SIZE,
    height: BLOB_SIZE,
    borderRadius: BLOB_SIZE / 2,
  },
  blob1: {
    backgroundColor: colors.redHot,
    top: -BLOB_SIZE / 2.5,
    left: -BLOB_SIZE / 2.5,
  },
  blob2: {
    backgroundColor: colors.redDeep,
    bottom: -BLOB_SIZE / 3,
    right: -BLOB_SIZE / 3,
  },
  slash: {
    position: 'absolute',
    width: 3,
    height: 280,
    backgroundColor: colors.redHot,
    opacity: 0.12,
    top: '50%',
    left: '50%',
    transform: [{ translateX: -1.5 }, { translateY: -140 }, { rotate: '-28deg' }],
  },
});
