import React, { useState } from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  StatusBar,
  Alert,
} from 'react-native';
import BackgroundLayer from '../components/BackgroundLayer';
import HeroIconWrap    from '../components/HeroIconWrap';
import BrandBlock      from '../components/BrandBlock';
import LoginCard       from '../components/LoginCard';
import SosStrip        from '../components/SosStrip';
import FeaturePills    from '../components/FeaturePills';
import { colors, spacing } from '../theme/tokens';

/**
 * RescueLinkScreen
 * Full-screen login/signup experience matching the HTML reference design.
 */
export default function RescueLinkScreen() {
  const [tab, setTab]             = useState<'login' | 'register'>('login');
  const [email, setEmail]         = useState('');
  const [password, setPassword]   = useState('');
  const [rememberMe, setRemember] = useState(false);
  const [isLoading, setLoading]   = useState(false);

  // Field error states
  const [emailError, setEmailError]       = useState<string | undefined>();
  const [passwordError, setPasswordError] = useState<string | undefined>();

  const validate = (): boolean => {
    let valid = true;
    if (!email.includes('@')) {
      setEmailError('Please enter a valid email address.');
      valid = false;
    } else {
      setEmailError(undefined);
    }
    if (password.length < 6) {
      setPasswordError('Password must be at least 6 characters.');
      valid = false;
    } else {
      setPasswordError(undefined);
    }
    return valid;
  };

  const handleSignIn = async () => {
    if (!validate()) return;
    setLoading(true);
    // TODO: wire to your auth service
    await new Promise(r => setTimeout(() => r(null), 1800));
    setLoading(false);
    Alert.alert('Welcome back!', `Signed in as ${email}`);
  };

  return (
    <View style={styles.root}>
      <StatusBar barStyle="light-content" backgroundColor={colors.blackCore} />

      {/* Atmospheric background */}
      <BackgroundLayer />

      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        keyboardVerticalOffset={Platform.OS === 'ios' ? 0 : 20}
      >
        <ScrollView
          contentContainerStyle={styles.scroll}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
        >
          {/* Hero Icon */}
          <HeroIconWrap />
          <View style={{ height: spacing.xxl }} />

          {/* Brand */}
          <BrandBlock />
          <View style={{ height: 36 }} />

          {/* Tab selector */}
          <View style={styles.tabRow}>
            {(['login', 'register'] as const).map(t => (
              <TouchableOpacity
                key={t}
                style={[styles.tab, tab === t && styles.tabActive]}
                onPress={() => setTab(t)}
                accessibilityRole="tab"
                accessibilityState={{ selected: tab === t }}
              >
                <Text style={[styles.tabText, tab === t && styles.tabTextActive]}>
                  {t === 'login' ? 'Sign In' : 'Create Account'}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
          <View style={{ height: 24 }} />

          {/* Login Card */}
          <LoginCard
            email={email}
            password={password}
            rememberMe={rememberMe}
            isLoading={isLoading}
            onEmailChange={setEmail}
            onPasswordChange={setPassword}
            onRememberMeToggle={() => setRemember(v => !v)}
            onSignIn={handleSignIn}
            onGoogleSignIn={() => Alert.alert('Google Sign-In', 'Not wired up yet.')}
            onForgotPassword={() => Alert.alert('Forgot Password', 'Reset link will be sent to your email.')}
            emailError={emailError}
            passwordError={passwordError}
          />

          {/* Sign-up row */}
          <View style={styles.signupRow}>
            <Text style={styles.signupText}>
              {tab === 'login' ? 'New to RescueLink? ' : 'Already have an account? '}
            </Text>
            <TouchableOpacity onPress={() => setTab(tab === 'login' ? 'register' : 'login')}>
              <Text style={styles.signupLink}>
                {tab === 'login' ? 'Create free account' : 'Sign in'}
              </Text>
            </TouchableOpacity>
          </View>

          {/* SOS Strip */}
          <View style={{ height: spacing.xxl }} />
          <SosStrip />

          {/* Feature Pills */}
          <View style={{ height: spacing.xxl }} />
          <FeaturePills />
          <View style={{ height: 40 }} />
        </ScrollView>
      </KeyboardAvoidingView>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: colors.blackCore,
  },
  flex: {
    flex: 1,
  },
  scroll: {
    paddingHorizontal: spacing.screen,
    paddingTop: 48,
    paddingBottom: 40,
    alignItems: 'center',
  },
  tabRow: {
    flexDirection: 'row',
    backgroundColor: colors.blackCard,
    borderRadius: 50,
    padding: 4,
    width: '100%',
  },
  tab: {
    flex: 1,
    paddingVertical: 10,
    alignItems: 'center',
    borderRadius: 50,
  },
  tabActive: {
    backgroundColor: colors.redHot,
  },
  tabText: {
    fontSize: 13,
    fontWeight: '600',
    color: 'rgba(255,255,255,0.6)',
  },
  tabTextActive: {
    color: '#fff',
  },
  signupRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: spacing.xl,
  },
  signupText: {
    fontSize: 13.5,
    color: colors.whiteDim,
  },
  signupLink: {
    fontSize: 13.5,
    color: colors.redHot,
    fontWeight: '600',
  },
});
