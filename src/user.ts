import { NativeModules } from 'react-native';

const tuya = NativeModules.TuyaUserModule;

export function registerAccountWithEmail(
  params: RegisterAccountWithEmailParams
): Promise<any> {
  return tuya.registerAccountWithEmail(params);
}

export function getRegisterEmailValidateCode(
  params: GetEmailValidateCodeParams
): Promise<any> {
  return tuya.getRegisterEmailValidateCode(params);
}

export function loginWithEmail(params: LoginWithEmailParams): Promise<any> {
  return tuya.loginWithEmail(params);
}

export function getEmailValidateCode(
  params: GetEmailValidateCodeParams
): Promise<any> {
  return tuya.getEmailValidateCode(params);
}

export function resetEmailPassword(
  params: ResetEmailPasswordParams
): Promise<any> {
  return tuya.resetEmailPassword(params);
}

export function logout(): Promise<string> {
  return tuya.logout();
}

export async function getCurrentUser(): Promise<User | null> {
  const user = await tuya.getCurrentUser();
  // The iOS SDK returns an empty user model but the Android one doesn't.
  return user && user.email ? user : null;
}

export function cancelAccount(): Promise<string> {
  return tuya.cancelAccount();
}

export function touristRegisterAndLogin(
  params: TouristRegisterAndLoginParams
): Promise<any> {
  return tuya.touristRegisterAndLogin(params);
}

export function touristBindWithUserName(
  params: TouristBindWithUserNameParams
): Promise<any> {
  return tuya.touristBindWithUserName(params);
}

export function sendVerifyCodeWithUserName(params: SendVerifyCodeWithUsername) {
  return tuya.sendVerifyCodeWithUserName(params);
}

export function registerAccountWithUid(
  params: RegisterAccountWithUidParams
): Promise<any> {
  return tuya.registerAccountWithUid(params);
}

export function loginWithUid(params: LoginWithUidParams): Promise<any> {
  return tuya.loginWithUid(params);
}

export function loginOrRegisterWithUid(
  params: LoginOrRegisterWithUidParams
): Promise<any> {
  return tuya.loginOrRegisterWithUid(params);
}

export function loginWithPhonePassword(
  params: LoginWithPhonePasswordParams
): Promise<any> {
  return tuya.loginWithPhonePassword(params);
}

export type LoginWithPhonePasswordParams = {
  countryCode: string;
  phoneNumber: string;
  password: string;
};

export type LoginWithUidParams = {
  countryCode: string;
  uid: string;
  password: string;
};

export type LoginOrRegisterWithUidParams = {
  countryCode: string;
  uid: string;
  password: string;
  isCreateHome: boolean;
};

export type RegisterAccountWithUidParams = {
  countryCode: string;
  uid: string;
  password: string;
};

export type SendVerifyCodeWithUsername = {
  countryCode: string;
  userName: string;
  region: string;
  type: number;
};

export type TouristRegisterAndLoginParams = {
  countryCode: string;
  nickName: string;
};

export type TouristBindWithUserNameParams = {
  countryCode: string;
  userName: string;
  verifyCode: string;
  password: string;
};

export type User = {
  email: string;
  username: string;
  sid: string;
  timezoneId: string;
  uid: string;
  userType: number;
  headPic: string;
  mobile: string;
  nickName: string;
  phoneCode: string;
};

export type RegisterAccountWithEmailParams = {
  countryCode: string;
  email: string;
  validateCode: string;
  password: string;
};

export type GetEmailValidateCodeParams = {
  countryCode: string;
  email: string;
};

export type LoginWithEmailParams = {
  email: string;
  password: string;
  countryCode: string;
};

export type ResetEmailPasswordParams = {
  email: string;
  countryCode: string;
  validateCode: string;
  newPassword: string;
};
