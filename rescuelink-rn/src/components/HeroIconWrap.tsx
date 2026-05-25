import React, { useEffect, useRef } from 'react';
import { View, StyleSheet, Animated, Easing } from 'react-native';
import { colors, animation } from '../theme/tokens';

/**
 * Hero icon with:
 * - Spinning conic-gradient ring (sweep effect via rotation)
 * - Car/warning SVG icon placeholder (View-based)
 * - Pulsing live dot (top-right)
 */
export default function HeroIconWrap() {
  const ringAnim  = useRef(new Animated.Value(0)).current;
  const dotAnim   = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    // Spin ring
    const spin = Animated.loop(
      Animated.timing(ringAnim, {
        toValue: 1,
        duration: animation.spinRing,
        easing: Easing.linear,
        useNativeDriver: true,
      })
    );
    // Pulse dot
    const pulse = Animated.loop(
      Animated.sequence([
        Animated.timing(dotAnim, {
          toValue: 1,
          duration: animation.pulse,
          easing: Easing.inOut(Easing.ease),
          useNativeDriver: true,
        }),
        Animated.timing(dotAnim, {
          toValue: 0,
          duration: animation.pulse,
          easing: Easing.inOut(Easing.ease),
          useNativeDriver: true,
        }),
      ])
    );
    spin.start();
    pulse.start();
    return () => { spin.stop(); pulse.stop(); };
  }, [ringAnim, dotAnim]);

  const ringRotate = ringAnim.interpolate({
    inputRange: [0, 1],
    outputRange: ['0deg', '360deg'],
  });
  const dotScale = dotAnim.interpolate({ inputRange: [0, 1], outputRange: [1, 1.15] });

  return (
    <View style={styles.wrap}>
      {/* Spinning gradient ring */}
      <Animated.View
        style={[styles.spinRing, { transform: [{ rotate: ringRotate }] }]}
      />

      {/* Inner hero box */}
      <View style={styles.heroBox}>
        {/* Warning / car icon (SVG-free fallback using emoji / Text) */}
        <View style={styles.iconInner}>
          {/* Red warning triangle shape made from views */}
          <View style={styles.warningIcon}>
            <View style={styles.warningBar} />
            <View style={styles.warningDot} />
          </View>
        </View>
      </View>

      {/* Pulse dot */}
      <Animated.View
        style={[styles.pulseDot, { transform: [{ scale: dotScale }] }]}
      />
    </View>
  );
}

const HERO = 88;
const RING = HERO + 4;

const styles = StyleSheet.create({
  wrap: {
    width: HERO + 16,
    height: HERO + 16,
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
  },
  spinRing: {
    position: 'absolute',
    width: RING,
    height: RING,
    borderRadius: 28,
    borderWidth: 2,
    borderColor: 'transparent',
    borderTopColor: colors.redHot,
    borderRightColor: colors.redHot + '40',
  },
  heroBox: {
    width: HERO,
    height: HERO,
    borderRadius: 26,
    backgroundColor: '#1E0007',
    borderWidth: 1.5,
    borderColor: colors.redHot + '59', // 35% opacity
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: colors.redHot,
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.4,
    shadowRadius: 20,
    elevation: 12,
  },
  iconInner: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  warningIcon: {
    alignItems: 'center',
    justifyContent: 'flex-end',
    width: 36,
    height: 36,
  },
  warningBar: {
    width: 4,
    height: 14,
    backgroundColor: colors.redHot,
    borderRadius: 2,
    marginBottom: 4,
  },
  warningDot: {
    width: 4,
    height: 4,
    backgroundColor: colors.redGlow,
    borderRadius: 2,
  },
  pulseDot: {
    position: 'absolute',
    top: 4,
    right: 4,
    width: 18,
    height: 18,
    borderRadius: 9,
    backgroundColor: colors.redHot,
    borderWidth: 2.5,
    borderColor: colors.blackCore,
    shadowColor: colors.redHot,
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.6,
    shadowRadius: 6,
    elevation: 6,
  },
});
