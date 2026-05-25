import React, { useRef } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Animated,
  ActivityIndicator,
  TextInput,
} from 'react-native';
import RescueLinkInput from './RescueLinkInput';
import { colors, spacing, radius, fontSize } from '../theme/tokens';

interface Props {
  email: string;
  password: string;
  rememberMe: boolean;
  isLoading: boolean;
  onEmailChange: (v: string) => void;
  onPasswordChange: (v: string) => void;
  onRememberMeToggle: () => void;
  onSignIn: () => void;
  onGoogleSignIn: () => void;
  onForgotPassword: () => void;
  emailError?: string;
  passwordError?: string;
}

/** The sign-in form card section */
export default function LoginCard({
  email, password, rememberMe, isLoading,
  onEmailChange, onPasswordChange,
  onRememberMeToggle, onSignIn, onGoogleSignIn, onForgotPassword,
  emailError, passwordError,
}: Props) {
  const passwordRef = useRef<TextInput>(null) as React.RefObject<TextInput>;
  const btnScale = useRef(new Animated.Value(1)).current;

  const pressIn  = () => Animated.spring(btnScale, { toValue: 0.97, useNativeDriver: true }).start();
  const pressOut = () => Animated.spring(btnScale, { toValue: 1.0, useNativeDriver: true }).start();

  return (
    <View style={styles.card}>
      {/* Top shimmer */}
      <View style={styles.topShimmer} />

      <Text style={styles.cardTitle}>Welcome back</Text>
      <Text style={styles.cardSub}>Sign in to get instant roadside help, 24/7.</Text>

      <View style={{ height: spacing.xxl }} />

      {/* Email */}
      <RescueLinkInput
        label="Email Address"
        placeholder="you@example.com"
        value={email}
        onChangeText={onEmailChange}
        error={emailError}
        returnKeyType="next"
        onSubmitEditing={() => passwordRef.current?.focus()}
        blurOnSubmit={false}
      />

      {/* Password */}
      <RescueLinkInput
        label="Password"
        placeholder="••••••••"
        value={password}
        onChangeText={onPasswordChange}
        isPassword
        error={passwordError}
        returnKeyType="done"
        onSubmitEditing={onSignIn}
        blurOnSubmit={true}
        inputRef={passwordRef}
      />

      {/* Remember / Forgot */}
      <View style={styles.metaRow}>
        <TouchableOpacity style={styles.remember} onPress={onRememberMeToggle} activeOpacity={0.8}>
          <View style={[styles.checkbox, rememberMe && styles.checkboxChecked]}>
            {rememberMe && <Text style={styles.checkmark}>✓</Text>}
          </View>
          <Text style={styles.rememberText}>Remember me</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={onForgotPassword}>
          <Text style={styles.forgot}>Forgot password?</Text>
        </TouchableOpacity>
      </View>

      {/* CTA */}
      <Animated.View style={{ transform: [{ scale: btnScale }] }}>
        <TouchableOpacity
          style={[styles.ctaBtn, isLoading && styles.ctaBtnDisabled]}
          onPressIn={pressIn}
          onPressOut={pressOut}
          onPress={onSignIn}
          disabled={isLoading}
          accessibilityRole="button"
          accessibilityLabel="Sign In to RescueLink"
        >
          {isLoading
            ? <ActivityIndicator color="#fff" size="small" />
            : <Text style={styles.ctaText}>Sign In to RescueLink</Text>
          }
        </TouchableOpacity>
      </Animated.View>

      {/* Divider */}
      <View style={styles.divider}>
        <View style={styles.dividerLine} />
        <Text style={styles.dividerText}>or continue with</Text>
        <View style={styles.dividerLine} />
      </View>

      {/* Google */}
      <TouchableOpacity
        style={styles.googleBtn}
        onPress={onGoogleSignIn}
        activeOpacity={0.8}
        accessibilityLabel="Continue with Google"
      >
        <View style={styles.googleIcon}>
          <Text style={styles.googleG}>G</Text>
        </View>
        <Text style={styles.googleText}>Continue with Google</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    width: '100%',
    backgroundColor: colors.blackCard,
    borderRadius: radius.card,
    borderWidth: 1,
    borderColor: colors.blackRim,
    padding: spacing.xxxl,
    paddingTop: spacing.xxxl + spacing.xs,
    overflow: 'hidden',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 24 },
    shadowOpacity: 0.6,
    shadowRadius: 32,
    elevation: 16,
  },
  topShimmer: {
    position: 'absolute',
    top: 0,
    left: '10%',
    right: '10%',
    height: 1,
    backgroundColor: colors.redHot,
    opacity: 0.6,
  },
  cardTitle: {
    fontSize: fontSize.cardTitle,
    fontWeight: '700',
    color: colors.whitePure,
    letterSpacing: -0.3,
  },
  cardSub: {
    fontSize: fontSize.cardSub,
    color: colors.whiteDim,
    marginTop: 6,
    lineHeight: 20,
  },
  metaRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: spacing.lg + spacing.sm,
    marginTop: -spacing.xs,
  },
  remember: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
  },
  checkbox: {
    width: 18,
    height: 18,
    borderRadius: 5,
    borderWidth: 1.5,
    borderColor: colors.blackRim,
    backgroundColor: colors.blackMid,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: spacing.sm,
  },
  checkboxChecked: {
    backgroundColor: colors.redHot,
    borderColor: colors.redHot,
  },
  checkmark: {
    color: '#fff',
    fontSize: 10,
    fontWeight: '700',
  },
  rememberText: {
    fontSize: 13,
    color: colors.whiteDim,
  },
  forgot: {
    fontSize: fontSize.forgot,
    color: colors.redHot,
    fontWeight: '600',
  },
  ctaBtn: {
    width: '100%',
    height: 56,
    borderRadius: radius.field,
    backgroundColor: colors.redHot,
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: colors.redHot,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.45,
    shadowRadius: 16,
    elevation: 8,
  },
  ctaBtnDisabled: {
    backgroundColor: colors.redDeep,
    opacity: 0.7,
  },
  ctaText: {
    color: '#fff',
    fontSize: fontSize.button,
    fontWeight: '700',
    letterSpacing: 0.5,
  },
  divider: {
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: spacing.xl,
  },
  dividerLine: {
    flex: 1,
    height: 1,
    backgroundColor: colors.blackRim,
  },
  dividerText: {
    fontSize: fontSize.divider,
    color: colors.whiteGhost,
    paddingHorizontal: spacing.md,
  },
  googleBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1.5,
    borderColor: colors.blackRim,
    backgroundColor: colors.blackMid,
    borderRadius: radius.field,
    height: 50,
    gap: spacing.sm,
  },
  googleIcon: {
    width: 20,
    height: 20,
    borderRadius: 10,
    backgroundColor: '#4285F4',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: spacing.sm,
  },
  googleG: {
    color: '#fff',
    fontSize: 11,
    fontWeight: '700',
  },
  googleText: {
    color: colors.whitePure,
    fontSize: 13,
    fontWeight: '600',
  },
});
