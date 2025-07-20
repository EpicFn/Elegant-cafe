import { useUser } from '@/src/store/auth';

export const useAuth = () => {
  const { user, login, clearUser, signup, updateUserInfo } = useUser();
  
  return {
    user,
    isAuthenticated: !!user,
    isAdmin: user?.isAdmin || false,
    login,
    logout: clearUser,
    signup,
    updateUser: updateUserInfo,
  };
}; 