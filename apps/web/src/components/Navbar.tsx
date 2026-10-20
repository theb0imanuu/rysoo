import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { fetchHealth } from '../services/api';
import { ShieldCheck, Activity, LogOut, User as UserIcon, Sparkles } from 'lucide-react';

interface NavbarProps {
  currentTab: 'dashboard' | 'studio' | 'architecture';
  onSelectTab: (tab: 'dashboard' | 'studio' | 'architecture') => void;
  onOpenUpload: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ currentTab, onSelectTab, onOpenUpload }) => {
  const { user, isDemo, signOut, loginWithDemo } = useAuth();
  const [apiOnline, setApiOnline] = useState<boolean | null>(null);

  useEffect(() => {
    let mounted = true;
    const checkApi = async () => {
      try {
        await fetchHealth();
        if (mounted) setApiOnline(true);
      } catch {
        if (mounted) setApiOnline(false);
      }
    };
    checkApi();
    const interval = setInterval(checkApi, 30000);
    return () => {
      mounted = false;
      clearInterval(interval);
    };
  }, []);

  return (
    <header className="navbar-container">
      <div className="navbar-content">
        <div className="navbar-left">
          <div className="navbar-brand" onClick={() => onSelectTab('dashboard')} role="button" tabIndex={0}>
            <div className="logo-wrapper">
              <img src="/logo.svg" alt="Rysoo Logo" className="brand-logo" onError={(e) => {
                // Fallback if SVG fails to load
                (e.currentTarget as HTMLImageElement).src = '/logo.png';
              }} />
            </div>
            <div className="brand-text">
              <span className="brand-name">RYSOO</span>
              <span className="brand-tagline">FACT-CERTIFIED ATS ENGINE</span>
            </div>
          </div>

          <nav className="navbar-nav">
            <button
              className={`nav-link ${currentTab === 'dashboard' ? 'active' : ''}`}
              onClick={() => onSelectTab('dashboard')}
            >
              Dashboard
            </button>
            <button
              className={`nav-link ${currentTab === 'studio' ? 'active' : ''}`}
              onClick={() => onSelectTab('studio')}
            >
              <Sparkles size={16} className="nav-icon" />
              Optimization Studio
            </button>
            <button
              className={`nav-link ${currentTab === 'architecture' ? 'active' : ''}`}
              onClick={() => onSelectTab('architecture')}
            >
              Architecture (Option A)
            </button>
          </nav>
        </div>

        <div className="navbar-right">
          {/* Health indicator */}
          <div className="api-status-badge" title="Spring Boot REST API Status (/api/v1/health)">
            <span className={`status-dot ${apiOnline === true ? 'online' : apiOnline === false ? 'offline' : 'checking'}`} />
            <Activity size={14} />
            <span>{apiOnline === true ? 'API Online' : apiOnline === false ? 'API Mock Mode' : 'Connecting...'}</span>
          </div>

          {/* Zero hallucination badge */}
          <div className="badge badge-success cert-badge" title="100% Deterministic Grounding Verified">
            <ShieldCheck size={14} />
            <span>Zero Hallucination</span>
          </div>

          <button className="btn-primary btn-sm" onClick={onOpenUpload}>
            + Ingest CV
          </button>

          {/* User profile / demo */}
          {user ? (
            <div className="user-profile-menu">
              <div className="user-avatar" title={user.email}>
                <UserIcon size={16} />
              </div>
              <div className="user-details">
                <span className="user-name">{user.name || 'Candidate'}</span>
                {isDemo && <span className="demo-pill">DEMO</span>}
              </div>
              <button
                className="btn-icon"
                onClick={signOut}
                title="Sign Out"
                aria-label="Sign Out"
              >
                <LogOut size={16} />
              </button>
            </div>
          ) : (
            <button className="btn-secondary btn-sm" onClick={loginWithDemo}>
              Demo Sign In
            </button>
          )}
        </div>
      </div>
    </header>
  );
};
