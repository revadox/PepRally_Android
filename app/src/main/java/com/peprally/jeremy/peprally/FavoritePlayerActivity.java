package com.peprally.jeremy.peprally;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FavoritePlayerActivity extends AppCompatActivity {

    private PaginatedQueryList<PlayerProfile> roster;
    private RecyclerView rv;
    private RVPlayersAdapter rvPlayersAdapter;
    private boolean dataFetched = false;

    private static final String TAG = FavoriteTeamActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_player);

        String team = getIntent().getStringExtra("TEAM");
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setTitle("UT " + team);

        rv = (RecyclerView) findViewById(R.id.rv_browse_players);
        LinearLayoutManager rvLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv.setHasFixedSize(true);
        rv.setLayoutManager(rvLayoutManager);

        Log.d(TAG, "loading players for: " + team);
        new FetchTeamRosterTask().execute(team);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dataFetched) {
            initializeAdapter(roster);
        }
    }

    private void initializeAdapter(PaginatedQueryList<PlayerProfile> result) {
        roster = result;
        rvPlayersAdapter = new RVPlayersAdapter(FavoritePlayerActivity.this, roster);
        rv.setAdapter(rvPlayersAdapter);
        rvPlayersAdapter.setOnItemClickListener(new RVPlayersAdapter.PlayersAdapterClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Log.d(TAG, roster.get(position).getFirstName());
            }
        });
    }

    private class FetchTeamRosterTask extends AsyncTask<String, Void, PaginatedQueryList<PlayerProfile>> {
        @Override
        protected PaginatedQueryList<PlayerProfile> doInBackground(String... params) {
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),                  // Context
                    AWSCredentialProvider.IDENTITY_POOL_ID,   // Identity Pool ID
                    AWSCredentialProvider.COGNITO_REGION      // Region
            );
            String team = params[0];
            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
            s3.setRegion(Region.getRegion(Regions.US_EAST_1));

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
            PlayerProfile playerProfile = new PlayerProfile();
            playerProfile.setTeam(team);
            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(playerProfile)
                    .withConsistentRead(false);
            PaginatedQueryList<PlayerProfile> result = mapper.query(PlayerProfile.class, queryExpression);
            return result;
        }

        @Override
        protected void onPostExecute(PaginatedQueryList<PlayerProfile> result) {
            dataFetched = true;
            initializeAdapter(result);
        }
    }
}
