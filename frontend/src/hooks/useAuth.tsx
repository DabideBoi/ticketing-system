"use client";

import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { clearToken, getToken, login as loginRequest, setToken } from "@/lib/api";
import type { User } from "@/lib/types";

interface AuthContextValue {
  user: User | null;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const USER_KEY = "ticketing_user";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = getToken();
    const storedUser = window.localStorage.getItem(USER_KEY);
    if (token && storedUser) {
      setUser(JSON.parse(storedUser) as User);
    }
    setIsLoading(false);
  }, []);

  async function login(email: string, password: string) {
    const response = await loginRequest(email, password);
    setToken(response.token);
    window.localStorage.setItem(USER_KEY, JSON.stringify(response.user));
    setUser(response.user);
  }

  function logout() {
    clearToken();
    window.localStorage.removeItem(USER_KEY);
    setUser(null);
    window.location.href = "/login";
  }

  return (
    <AuthContext.Provider value={{ user, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
