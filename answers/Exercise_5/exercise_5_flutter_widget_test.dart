/*
Exercise 5 notes:

I focused on the main behavior of the LoginForm widget:
- fields are visible
- required validation works
- invalid email validation works
- submit calls AuthService when data is valid
- loading state is shown while login is running
- onSuccess is called after successful login
- error SnackBar is shown after failed login

I kept the fake AuthService simple because the goal here is widget behavior,
not testing the real backend login implementation.
*/

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

class FakeAuthService implements AuthService {
  bool shouldSucceed;
  bool wasCalled = false;
  String? lastEmail;
  String? lastPassword;
  Duration delay;

  FakeAuthService({
    this.shouldSucceed = true,
    this.delay = Duration.zero,
  });

  @override
  Future<void> login(String email, String password) async {
    wasCalled = true;
    lastEmail = email;
    lastPassword = password;

    if (delay != Duration.zero) {
      await Future.delayed(delay);
    }

    if (!shouldSucceed) {
      throw Exception('Invalid credentials');
    }
  }
}

Widget buildTestWidget({
  required AuthService authService,
  VoidCallback? onSuccess,
}) {
  return MaterialApp(
    home: Scaffold(
      body: LoginForm(
        authService: authService,
        onSuccess: onSuccess ?? () {},
      ),
    ),
  );
}

void main() {
  testWidgets('shows email and password fields', (WidgetTester tester) async {
    final authService = FakeAuthService();

    await tester.pumpWidget(buildTestWidget(authService: authService));

    expect(find.byType(TextFormField), findsNWidgets(2));
    expect(find.text('Login'), findsOneWidget);
  });

  testWidgets('shows validation messages when submitting empty form', (WidgetTester tester) async {
    final authService = FakeAuthService();

    await tester.pumpWidget(buildTestWidget(authService: authService));

    await tester.tap(find.text('Login'));
    await tester.pump();

    expect(find.textContaining('email'), findsWidgets);
    expect(find.textContaining('password'), findsWidgets);
    expect(authService.wasCalled, isFalse);
  });

  testWidgets('shows validation message for invalid email', (WidgetTester tester) async {
    final authService = FakeAuthService();

    await tester.pumpWidget(buildTestWidget(authService: authService));

    await tester.enterText(find.byType(TextFormField).at(0), 'wrong-email');
    await tester.enterText(find.byType(TextFormField).at(1), 'password123');

    await tester.tap(find.text('Login'));
    await tester.pump();

    expect(find.textContaining('valid'), findsWidgets);
    expect(authService.wasCalled, isFalse);
  });

  testWidgets('calls auth service with entered email and password', (WidgetTester tester) async {
    final authService = FakeAuthService();

    await tester.pumpWidget(buildTestWidget(authService: authService));

    await tester.enterText(find.byType(TextFormField).at(0), 'qa@example.com');
    await tester.enterText(find.byType(TextFormField).at(1), 'password123');

    await tester.tap(find.text('Login'));
    await tester.pumpAndSettle();

    expect(authService.wasCalled, isTrue);
    expect(authService.lastEmail, 'qa@example.com');
    expect(authService.lastPassword, 'password123');
  });

  testWidgets('shows loading indicator while login is running', (WidgetTester tester) async {
    final authService = FakeAuthService(delay: const Duration(seconds: 1));

    await tester.pumpWidget(buildTestWidget(authService: authService));

    await tester.enterText(find.byType(TextFormField).at(0), 'qa@example.com');
    await tester.enterText(find.byType(TextFormField).at(1), 'password123');

    await tester.tap(find.text('Login'));
    await tester.pump();

    expect(find.byType(CircularProgressIndicator), findsOneWidget);

    await tester.pumpAndSettle();
  });

  testWidgets('calls onSuccess after successful login', (WidgetTester tester) async {
    final authService = FakeAuthService();
    bool successCalled = false;

    await tester.pumpWidget(
      buildTestWidget(
        authService: authService,
        onSuccess: () {
          successCalled = true;
        },
      ),
    );

    await tester.enterText(find.byType(TextFormField).at(0), 'qa@example.com');
    await tester.enterText(find.byType(TextFormField).at(1), 'password123');

    await tester.tap(find.text('Login'));
    await tester.pumpAndSettle();

    expect(successCalled, isTrue);
  });

  testWidgets('shows SnackBar when login fails', (WidgetTester tester) async {
    final authService = FakeAuthService(shouldSucceed: false);

    await tester.pumpWidget(buildTestWidget(authService: authService));

    await tester.enterText(find.byType(TextFormField).at(0), 'qa@example.com');
    await tester.enterText(find.byType(TextFormField).at(1), 'bad-password');

    await tester.tap(find.text('Login'));
    await tester.pumpAndSettle();

    expect(find.byType(SnackBar), findsOneWidget);
    expect(find.textContaining('Invalid'), findsWidgets);
  });
}