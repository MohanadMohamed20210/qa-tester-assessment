<?php

namespace Tests\Feature;

use App\Models\Gift;
use App\Models\Room;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

/*
Exercise 3 notes:

The original test was too weak because it only checked that the API returned 200.
For a wallet/gift endpoint, that is not enough.

Main issues I noticed:
- It used a fake bearer token instead of authenticating the user properly.
- It did not check that the sender wallet was debited.
- It did not check that a gift transaction was created.
- It did not check duplicate retry/idempotency behavior.
- It did not reset the database between tests.

I kept the refactor simple because I would normally confirm the real model names,
table names, and response format with the backend team before making this final.
*/

class SendGiftTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_send_gift_and_wallet_is_debited(): void
    {
        $sender = User::factory()->create([
            'coins' => 10000,
        ]);

        $room = Room::factory()->create();

        $gift = Gift::factory()->create([
            'coin_price' => 500,
        ]);

        Sanctum::actingAs($sender);

        $response = $this->postJson('/api/gifts/send', [
            'room_id' => $room->id,
            'gift_id' => $gift->id,
            'quantity' => 2,
            'winners_count' => 1,
            'idempotency_key' => '11111111-1111-4111-8111-111111111111',
        ]);

        $response->assertStatus(200);

        $sender->refresh();

        $this->assertEquals(
            9000,
            $sender->coins,
            'Sender wallet should be debited by gift price multiplied by quantity.'
        );

        $this->assertDatabaseHas('gift_transactions', [
            'sender_id' => $sender->id,
            'room_id' => $room->id,
            'gift_id' => $gift->id,
            'quantity' => 2,
            'coins_charged' => 1000,
        ]);
    }

    public function test_guest_cannot_send_gift(): void
    {
        $room = Room::factory()->create();
        $gift = Gift::factory()->create();

        $response = $this->postJson('/api/gifts/send', [
            'room_id' => $room->id,
            'gift_id' => $gift->id,
            'quantity' => 1,
            'winners_count' => 1,
            'idempotency_key' => '22222222-2222-4222-8222-222222222222',
        ]);

        $response->assertStatus(401);
    }

    public function test_same_idempotency_key_does_not_charge_twice(): void
    {
        $sender = User::factory()->create([
            'coins' => 10000,
        ]);

        $room = Room::factory()->create();

        $gift = Gift::factory()->create([
            'coin_price' => 500,
        ]);

        Sanctum::actingAs($sender);

        $payload = [
            'room_id' => $room->id,
            'gift_id' => $gift->id,
            'quantity' => 2,
            'winners_count' => 1,
            'idempotency_key' => '33333333-3333-4333-8333-333333333333',
        ];

        $firstResponse = $this->postJson('/api/gifts/send', $payload);
        $secondResponse = $this->postJson('/api/gifts/send', $payload);

        $firstResponse->assertStatus(200);

        /*
        The second response may be 200 with the same original result,
        or 409 depending on the real API decision.
        The important check here is that the wallet is not charged twice.
        */
        $this->assertContains($secondResponse->status(), [200, 409]);

        $sender->refresh();

        $this->assertEquals(
            9000,
            $sender->coins,
            'Using the same idempotency key should not debit the wallet twice.'
        );

        $this->assertDatabaseCount('gift_transactions', 1);
    }
}