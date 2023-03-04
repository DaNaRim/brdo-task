import React, {useEffect} from "react";
import {useNavigate} from "react-router";
import {NavLink} from "react-router-dom";
import {useAppDispatch, useAppSelector} from "../../../../app/hooks";
import {
    useAuthGetStateMutation,
    useAuthRefreshMutation,
    useLogoutMutation,
} from "../../../../features/auth/authApiSlice";
import {
    clearAuthState,
    selectAuthIsInitialized,
    selectAuthUsername,
    setCredentials,
    setInitialized,
} from "../../../../features/auth/authSlice";

import styles from "./Header.module.scss";

const Header = () => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    const username = useAppSelector(selectAuthUsername);

    const isAuthInit = useAppSelector(selectAuthIsInitialized);

    const [getAuthState, {isLoading: isAuthStateLoading}] = useAuthGetStateMutation();
    const [requestAuth, {isLoading: isRequestAuthLoading}] = useAuthRefreshMutation();
    const [logout, {isLoading: isLogoutLoading}] = useLogoutMutation();

    const handleLogout = () => {
        logout();
        dispatch(clearAuthState());
        navigate("/login");
    };

    useEffect(() => {
        if (!isAuthInit && !isAuthStateLoading) {
            getAuthState().unwrap()
                .then(res => dispatch(setCredentials(res)))
                .catch(() => {
                    logout();
                    dispatch(clearAuthState());
                    dispatch(setInitialized());
                });
        }
    }, [dispatch, getAuthState, isAuthInit]);

    const getAuthBlock = () => {
        if (isAuthStateLoading || isRequestAuthLoading || isLogoutLoading) {
            return <div>Loading...</div>;
        } else if (username) {
            return <div>
                <p>You are Logged in</p>
                <button id="logoutButton" onClick={handleLogout}>Logout</button>
            </div>;
        } else {
            return <ul>
                <li><NavLink to="/login">Login</NavLink></li>
            </ul>;
        }
    };

    return (
        <header className={styles.main_header}>
            <nav>
                <ul>
                    <li><NavLink to="/">Home</NavLink></li>
                </ul>
            </nav>
            <div className="logoBlock">

            </div>
            <div className="authBlock">
                {getAuthBlock()}
            </div>
        </header>
    );
};

export default Header;
