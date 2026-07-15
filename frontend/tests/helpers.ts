import type { Page } from "@playwright/test";

export const SEEDED_PASSWORD = "password123";

export const SEEDED_USERS = {
  requestor: "requestor@ticketing.local",
  approver: "approver@ticketing.local",
  assigner: "assigner@ticketing.local",
  assignee: "assignee@ticketing.local",
  admin: "admin@ticketing.local",
} as const;

export async function loginAs(page: Page, email: string) {
  await page.goto("/login");
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill(SEEDED_PASSWORD);
  await page.getByRole("button", { name: "Sign in" }).click();
  await page.waitForURL("**/dashboard");
}

export async function logout(page: Page) {
  await page.getByLabel("Logout").click();
  await page.waitForURL("**/login");
}
