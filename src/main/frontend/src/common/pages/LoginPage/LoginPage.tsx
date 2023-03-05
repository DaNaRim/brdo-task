import React, {useState} from "react";
import {useForm} from "react-hook-form";
import {useNavigate} from "react-router";
import {Link} from "react-router-dom";
import {useAppDispatch} from "../../../app/hooks";
import {Credentials, useLoginMutation} from "../../../features/auth/authApiSlice";
import {setCredentials} from "../../../features/auth/authSlice";
import {clearFormSystemFields, ErrorResponse, FormSystemFields, handleResponseError} from "../../utils/FormUtils";
import styles from "./LoginPage.module.scss";


type LoginFormFields = FormSystemFields & Credentials

type LoginFormError = ErrorResponse & {
    fieldName: keyof LoginFormFields,
}

const LoginPage = () => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    const {register, handleSubmit, setValue, setError, formState: {errors}} = useForm<LoginFormFields>();

    const [isAccountNotActivated, setIsAccountNotActivated] = useState<boolean>(false);

    const [login, {isLoading}] = useLoginMutation();

    const handleLogin = (data: LoginFormFields) => {
        clearFormSystemFields(data);

        login(data).unwrap()
            .then(data => dispatch(setCredentials(data)))
            .then(() => {
                navigate("/comments");
            })
            .catch(e => {
                setValue("password", "");

                const errorData: LoginFormError[] = e.data;

                if (typeof errorData === "object"
                    && errorData.some(error => error.errorCode === "validation.auth.disabled")) {

                    setIsAccountNotActivated(true);
                }
                handleResponseError(e, setError);
            });
    };

    return (
        <main className={styles.login_page}>
            <h1>Login page</h1>

            <p className={styles.hint}>Username: <span>test@test.test</span></p>
            <p className={styles.hint}>Password: <span>12345678</span></p>

            <form onSubmit={handleSubmit(handleLogin)}>
                <label htmlFor="username">Email: </label>
                <input type="email" id="username" {...register("username", {required: true})}/>
                {errors.username?.type === "required" && <span>Email is required</span>}
                {errors.username && <span>{errors.username.message}</span>}<br/>

                <label htmlFor="password">Password: </label>
                <input type="password" id="password" {...register("password", {required: true})}/>
                {errors.password?.type === "required" && <span>Password is required</span>}
                {errors.password && <span>{errors.password.message}</span>}<br/>

                <input type="hidden" {...register("globalError")}/>
                {errors.globalError && <span>{errors.globalError.message}</span>}<br/>
                {isAccountNotActivated && <Link to={"/resendVerificationToken"}>Resend verification token</Link>}

                <input type="hidden" {...register("serverError")}/>
                {errors.serverError && <span className={styles.server_error}>{errors.serverError.message}</span>}
                <br/>

                {isLoading
                    ? <span>Loading...</span>
                    : <button type="submit">Login</button>
                }
            </form>
        </main>
    );
};

export default LoginPage;
