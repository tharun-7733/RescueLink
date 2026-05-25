import React, { useRef, useEffect, useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Animated,
  Alert,
  Linking,
} from 'react-native';
import { colors, spacing, radius, fontSize, animation } from '../theme/tokens';

/** Pulsing SOS emergency strip with emergency call dialog */
export default function SosStrip() {
  const badgeScale = useRef(new Animated.Value(1)).current;
  const [isPressed, setIsPressed] = useState(false);
  const bgAnim = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    const pulse = Animated.loop(
      Animated.sequence([
        Animated.timing(badgeScale, {
          toValue: 1.1,
          duration: animation.pulse,
          useNativeDriver: true,
        }),
        Animated.timing(badgeScale, {
          toValue: 1,
          duration: animation.pulse,
          useNativeDriver: true,
        }),
      ])
    );
    pulse.start();
    return () => pulse.stop();
  }, [badgeScale]);

  const handlePress = () => {
    // Flash animation
    Animated.sequence([
      Animated.timing(bgAnim, { toValue: 1, duration: 150, useNativeDriver: false }),
      Animated.delay(300),
      Animated.timing(bgAnim, { toValue: 0, duration: 200, useNativeDriver: false }),
    ]).start();

    Alert.alert(
      'Emergency Help',
      'Choose how to get immediate assistance:',
      [
        {
          text: 'Call 112',
          style: 'destructive',
          onPress: () => Linking.openURL('tel:112'),
        },
        {
          text: 'Call 911',
          style: 'destructive',
          onPress: () => Linking.openURL('tel:911'),
        },
        { text: 'Cancel', style: 'cancel' },
      ]
    );
  };

  const stripBg = bgAnim.interpolate({
    inputRange: [0, 1],
    outputRange: ['rgba(232,0,29,0.07)', 'rgba(232,0,29,0.25)'],
  });
  const stripBorder = bgAnim.interpolate({
    inputRange: [0, 1],
    outputRange: ['rgba(232,0,29,0.18)', 'rgba(232,0,29,0.7)'],
  });

  return (
    <Animated.View style={[styles.strip, { backgroundColor: stripBg, borderColor: stripBorder }]}>
      <TouchableOpacity
        style={styles.touchable}
        onPress={handlePress}
        activeOpacity={0.9}
        accessibilityRole="button"
        accessibilityLabel="SOS Emergency Help"
      >
        {/* Pulsing badge */}
        <Animated.View style={[styles.badge, { transform: [{ scale: badgeScale }] }]}>
          <Text style={styles.badgeText}>SOS</Text>
        </Animated.View>

        {/* Text */}
        <View style={styles.textBlock}>
          <Text style={styles.title}>Immediate Emergency Help</Text>
          <Text style={styles.sub}>Tap to call without logging in</Text>
        </View>

        {/* Chevron */}
        <Text style={styles.chevron}>›</Text>
      </TouchableOpacity>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  strip: {
    width: '100%',
    borderWidth: 1,
    borderRadius: radius.strip,
    overflow: 'hidden',
  },
  touchable: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 18,
    paddingVertical: 14,
    gap: 14,
  },
  badge: {
    width: 36,
    height: 36,
    borderRadius: radius.badge,
    backgroundColor: colors.redHot,
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: colors.redHot,
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.5,
    shadowRadius: 7,
    elevation: 6,
  },
  badgeText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '700',
    letterSpacing: 1,
  },
  textBlock: {
    flex: 1,
  },
  title: {
    fontSize: fontSize.sosTitle,
    fontWeight: '700',
    color: colors.whitePure,
  },
  sub: {
    fontSize: fontSize.sosSub,
    color: colors.whiteDim,
    marginTop: 1,
  },
  chevron: {
    fontSize: 22,
    color: 'rgba(255,255,255,0.35)',
  },
});
