import React, { createContext, useContext, useEffect, useState } from 'react';
import { supabase, isConfigured } from '../services/supabase';

export interface User {
  id: string;
  email: string;
  name?: string;
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  loginWithDemo: () => void;
  signOut: () => Promise<void>;
  isDemo: boolean;
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  loading: true,
  loginWithDemo: () => {},
  signOut: async () => {},
  isDemo: false,
});

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [isDemo, setIsDemo] = useState(false);

  useEffect(() => {
    // Check local storage for demo user
    const savedDemo = localStorage.getItem('rysoo_demo_user');
    if (savedDemo) {
      try {
        setUser(JSON.parse(savedDemo));
        setIsDemo(true);
        setLoading(false);
        return;
      } catch {
        localStorage.removeItem('rysoo_demo_user');
      }
    }

    if (isConfigured) {
      supabase.auth.getSession().then(({ data }) => {
        if (data?.session?.user) {
          setUser({
            id: data.session.user.id,
            email: data.session.user.email || 'user@example.com',
            name: data.session.user.user_metadata?.full_name || 'Candidate',
          });
        }
        setLoading(false);
      });

      const { data: authListener } = supabase.auth.onAuthStateChange((_event, session) => {
        if (session?.user) {
          setUser({
            id: session.user.id,
            email: session.user.email || 'user@example.com',
            name: session.user.user_metadata?.full_name || 'Candidate',
          });
        } else if (!localStorage.getItem('rysoo_demo_user')) {
          setUser(null);
        }
      });

      return () => {
        authListener.subscription.unsubscribe();
      };
    } else {
      // Default to demo user if Supabase is unconfigured
      loginWithDemo();
      setLoading(false);
    }
  }, []);

  const loginWithDemo = () => {
    const demoUser: User = {
      id: '00000000-0000-0000-0000-000000000001',
      email: 'jane.doe.synthetic@example.com',
      name: 'Jane Doe',
    };
    setUser(demoUser);
    setIsDemo(true);
    localStorage.setItem('rysoo_demo_user', JSON.stringify(demoUser));
  };

  const signOut = async () => {
    if (isConfigured) {
      await supabase.auth.signOut();
    }
    localStorage.removeItem('rysoo_demo_user');
    setUser(null);
    setIsDemo(false);
  };

  return (
    <AuthContext.Provider value={{ user, loading, loginWithDemo, signOut, isDemo }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
