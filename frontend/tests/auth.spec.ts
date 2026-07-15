import { test, expect } from "@playwright/test";
import { SEEDED_USERS, SEEDED_PASSWORD, loginAs, logout } from "./helpers";

test.describe("Authentication", () => {
  test("valid credentials redirect to the dashboard", async ({ page }) => {
    await loginAs(page, SEEDED_USERS.admin);
    await expect(page).toHaveURL(/\/dashboard/);
    await expect(page.locator("header").getByText("Alex Admin")).toBeVisible();
  });

  test("invalid credentials show an error and stay on login", async ({ page }) => {
    await page.goto("/login");
    await page.getByLabel("Email").fill(SEEDED_USERS.admin);
    await page.getByLabel("Password").fill("wrong-password");
    await page.getByRole("button", { name: "Sign in" }).click();

    await expect(page.getByText(/invalid credentials/i)).toBeVisible();
    await expect(page).toHaveURL(/\/login/);
  });

  test("visiting a protected route while logged out redirects to login", async ({ page }) => {
    await page.goto("/dashboard");
    await page.waitForURL("**/login");
  });

  test("logout clears the session and redirects to login", async ({ page }) => {
    await loginAs(page, SEEDED_USERS.admin);
    await logout(page);
    await expect(page).toHaveURL(/\/login/);

    await page.goto("/dashboard");
    await page.waitForURL("**/login");
  });
});
