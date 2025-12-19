import importlib
import pytest
from flask import Flask
from unittest.mock import MagicMock
from src.repository import User, Expense
from src.api import auth
import src.api.expense_controller as expense_controller

@pytest.fixture
def app(monkeypatch):
  def identity_decorator(fn):
    return fn

  # mock require_employee_auth from auth
  monkeypatch.setattr(auth, "require_employee_auth", identity_decorator)

  # reload imports
  expense_controller_module = importlib.reload(expense_controller)

  app = Flask(__name__)
  app.testing = True
  app.register_blueprint(expense_controller_module.expense_bp)

  return app

@pytest.fixture
def client(app):
    return app.test_client()

def test_submit_expense(client, app, monkeypatch):
  # sample data
  fake_user = User(1, "test_user", "test_pass", "Employee")
  fake_expense = Expense(101, 1, 100.1, "test_description", "2025-12-19")

  # Mock authenticated user
  monkeypatch.setattr(
    expense_controller,
    "get_current_user",
    lambda: fake_user
  )

  # Mock ExpenseService
  mock_service = MagicMock()
  mock_service.submit_expense.return_value = fake_expense
  app.expense_service = mock_service

  response = client.post(
    "/api/expenses",
    json={
      "amount": 100.1,
      "description": "test_description",
      "date": "2025-12-19"
    }
  )

  assert response.status_code == 201
  mock_service.submit_expense.assert_called_once_with(
    user_id=1,
    amount=100.1,
    description="test_description",
    date="2025-12-19"
  )